package com.example.data.pocketbase

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.R
import com.example.data.model.PbOAuth2Provider
import com.example.data.model.PbUserSession
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

private val Context.pbSessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "surabaya_pocketbase_auth")

class PocketBaseException(
    val statusCode: Int,
    override val message: String,
) : IOException(message)

data class MultipartFilePart(
    val fieldName: String,
    val fileName: String,
    val mimeType: String = "image/jpeg",
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartFilePart) return false
        return fieldName == other.fieldName &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fieldName.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

class PocketBaseClient(
    baseUrlInput: String,
    private val dataStore: DataStore<Preferences>? = null,
    httpClientOverride: OkHttpClient? = null,
    private val enableRealtimeSse: Boolean = true,
) {
    @Volatile
    var baseUrl: String = baseUrlInput.trim().trimEnd('/')
        private set

    private val httpClient: OkHttpClient = httpClientOverride ?: OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()

    private val sseClient: OkHttpClient = httpClient.newBuilder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sseJob: Job? = null

    private val _sessionFlow = MutableStateFlow<PbUserSession?>(null)
    val sessionFlow: StateFlow<PbUserSession?> = _sessionFlow.asStateFlow()

    // Emits collection names whenever local mutations OR PocketBase /api/realtime SSE events arrive
    private val changeSignals = MutableSharedFlow<String>(extraBufferCapacity = 64)

    val currentSession: PbUserSession?
        get() = _sessionFlow.value

    init {
        if (dataStore != null) {
            clientScope.launch {
                restorePersistedSession()
            }
        }
    }

    suspend fun restorePersistedSession(): PbUserSession? = withContext(Dispatchers.IO) {
        val ds = dataStore ?: return@withContext _sessionFlow.value
        val prefs = ds.data.first()
        val savedUrl = prefs[KEY_BASE_URL]
        if (!savedUrl.isNullOrBlank()) {
            baseUrl = savedUrl.trim().trimEnd('/')
        }
        val token = prefs[KEY_TOKEN].orEmpty()
        val uid = prefs[KEY_USER_ID].orEmpty()
        if (token.isNotBlank() && uid.isNotBlank()) {
            val restored = PbUserSession(
                token = token,
                userId = uid,
                email = prefs[KEY_EMAIL].orEmpty(),
                displayName = prefs[KEY_DISPLAY_NAME].orEmpty().ifBlank { "Warga Surabaya" },
                district = prefs[KEY_DISTRICT].orEmpty().ifBlank { "Genteng" },
                isAdmin = prefs[KEY_IS_ADMIN] ?: false,
            )
            _sessionFlow.value = restored
            if (enableRealtimeSse) startRealtimeStream()
            // Silently refresh token in background if server is reachable
            try {
                refreshAuth()
            } catch (e: PocketBaseException) {
                if (e.statusCode == 401) {
                    signOut()
                    return@withContext null
                }
            } catch (_: Exception) {
                // Keep offline/transient session alive
            }
            return@withContext _sessionFlow.value
        }
        return@withContext null
    }

    suspend fun updateServerUrl(newUrl: String) {
        val cleaned = newUrl.trim().trimEnd('/')
        if (cleaned.isBlank()) return
        baseUrl = cleaned
        dataStore?.edit { it[KEY_BASE_URL] = cleaned }
        if (_sessionFlow.value != null && enableRealtimeSse) {
            startRealtimeStream()
        }
    }

    suspend fun checkHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$baseUrl/api/health")
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkCollectionsReady(): Boolean = withContext(Dispatchers.IO) {
        try {
            val req = Request.Builder()
                .url("$baseUrl/api/collections/chat_rooms/records?perPage=1")
                .applyAuth()
                .get()
                .build()
            httpClient.newCall(req).execute().use { resp ->
                // 200 or 400/403 (auth required) means the collection exists; 404 means collection is missing!
                resp.code != 404
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun registerAndSignIn(
        name: String,
        email: String,
        password: String,
        district: String = "Genteng",
    ): PbUserSession = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim().ifBlank { cleanEmail.substringBefore("@") }.take(80)
        val isBootstrappedAdmin = cleanEmail.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)

        val registerBody = JSONObject().apply {
            put("email", cleanEmail)
            put("emailVisibility", true)
            put("password", password)
            put("passwordConfirm", password)
            put("name", cleanName)
            put("district", district)
            put("bio", "Warga aktif Arek Surabaya")
            put("search_radius_km", 10.0)
            put("is_discoverable", true)
            put("is_admin", isBootstrappedAdmin)
        }
        executeJson("POST", "/api/collections/users/records", registerBody, includeAuth = false)
        signInWithPassword(cleanEmail, password)
    }

    suspend fun signInWithPassword(
        identity: String,
        password: String,
    ): PbUserSession = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("identity", identity.trim())
            put("password", password)
        }
        val json = executeJson("POST", "/api/collections/users/auth-with-password", body, includeAuth = false)
        val session = parseAuthSession(json)
        saveSession(session)
        if (enableRealtimeSse) startRealtimeStream()
        notifyCollectionChanged("*")
        session
    }

    suspend fun fetchOAuth2Providers(): List<PbOAuth2Provider> = withContext(Dispatchers.IO) {
        val json = executeJson("GET", "/api/collections/users/auth-methods", null, includeAuth = false)
        val arr = json.optJSONArray("authProviders") ?: JSONArray()
        buildList(arr.length()) {
            for (i in 0 until arr.length()) {
                val p = arr.optJSONObject(i) ?: continue
                add(
                    PbOAuth2Provider(
                        name = p.optString("name"),
                        displayName = p.optString("displayName").ifBlank { p.optString("name") },
                        state = p.optString("state"),
                        codeVerifier = p.optString("codeVerifier"),
                        codeChallenge = p.optString("codeChallenge"),
                        authUrl = p.optString("authUrl"),
                    ),
                )
            }
        }
    }

    suspend fun savePendingOAuth2Flow(
        providerName: String,
        codeVerifier: String,
        state: String,
        redirectUrl: String = OAUTH2_REDIRECT_URI,
    ) = withContext(Dispatchers.IO) {
        dataStore?.edit { prefs ->
            prefs[KEY_OAUTH_PROVIDER] = providerName
            prefs[KEY_OAUTH_VERIFIER] = codeVerifier
            prefs[KEY_OAUTH_STATE] = state
            prefs[KEY_OAUTH_REDIRECT] = redirectUrl
        }
    }

    suspend fun completePendingOAuth2FromCallback(
        code: String,
        returnedState: String?,
    ): PbUserSession = withContext(Dispatchers.IO) {
        val ds = dataStore ?: throw PocketBaseException(400, "Sesi OAuth2 lokal tidak tersedia.")
        val prefs = ds.data.first()
        val provider = prefs[KEY_OAUTH_PROVIDER].orEmpty()
        val verifier = prefs[KEY_OAUTH_VERIFIER].orEmpty()
        val expectedState = prefs[KEY_OAUTH_STATE].orEmpty()
        val redirectUrl = prefs[KEY_OAUTH_REDIRECT].orEmpty().ifBlank { OAUTH2_REDIRECT_URI }

        if (provider.isBlank() || verifier.isBlank()) {
            throw PocketBaseException(400, "Sesi login sosial telah kedaluwarsa. Silakan ulangi klik ikon login.")
        }
        if (expectedState.isNotBlank() && !returnedState.isNullOrBlank() && expectedState != returnedState) {
            throw PocketBaseException(400, "Validasi keamanan state OAuth2 tidak cocok. Silakan coba lagi.")
        }

        val body = JSONObject().apply {
            put("provider", provider)
            put("code", code.trim())
            put("codeVerifier", verifier)
            put("redirectUrl", redirectUrl)
            put(
                "createData",
                JSONObject().apply {
                    put("district", "Genteng")
                    put("bio", "Warga aktif Arek Surabaya")
                    put("search_radius_km", 10.0)
                    put("is_discoverable", true)
                },
            )
        }
        val json = executeJson("POST", "/api/collections/users/auth-with-oauth2", body, includeAuth = false)
        ds.edit { editPrefs ->
            editPrefs.remove(KEY_OAUTH_PROVIDER)
            editPrefs.remove(KEY_OAUTH_VERIFIER)
            editPrefs.remove(KEY_OAUTH_STATE)
            editPrefs.remove(KEY_OAUTH_REDIRECT)
        }
        val session = parseAuthSession(json)
        saveSession(session)
        if (enableRealtimeSse) startRealtimeStream()
        notifyCollectionChanged("*")
        session
    }

    suspend fun refreshAuth(): PbUserSession? = withContext(Dispatchers.IO) {
        val current = _sessionFlow.value ?: return@withContext null
        if (current.token.isBlank()) return@withContext null
        val json = executeJson("POST", "/api/collections/users/auth-refresh", JSONObject(), includeAuth = true)
        val updated = parseAuthSession(json)
        saveSession(updated)
        updated
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        sseJob?.cancel()
        sseJob = null
        _sessionFlow.value = null
        dataStore?.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_DISTRICT)
            prefs.remove(KEY_IS_ADMIN)
        }
    }

    private suspend fun saveSession(session: PbUserSession) {
        _sessionFlow.value = session
        dataStore?.edit { prefs ->
            prefs[KEY_BASE_URL] = baseUrl
            prefs[KEY_TOKEN] = session.token
            prefs[KEY_USER_ID] = session.userId
            prefs[KEY_EMAIL] = session.email
            prefs[KEY_DISPLAY_NAME] = session.displayName
            prefs[KEY_DISTRICT] = session.district
            prefs[KEY_IS_ADMIN] = session.isAdmin
        }
    }

    private fun parseAuthSession(authResponse: JSONObject): PbUserSession {
        val token = authResponse.optString("token")
        val record = authResponse.optJSONObject("record") ?: JSONObject()
        val email = record.optString("email")
        val rawName = record.optString("name").ifBlank {
            record.optString("username").ifBlank { email.substringBefore("@").ifBlank { "Warga Surabaya" } }
        }
        val district = record.optString("district").ifBlank { "Genteng" }
        val isAdminFlag = record.optBoolean("is_admin", false) ||
            email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)
        return PbUserSession(
            token = token,
            userId = record.optString("id"),
            email = email,
            displayName = rawName,
            district = district,
            isAdmin = isAdminFlag,
        )
    }

    // --- GENERIC POCKETBASE REST CRUD ---

    suspend fun getList(
        collection: String,
        filter: String? = null,
        sort: String? = null,
        perPage: Int = 100,
    ): List<JSONObject> = withContext(Dispatchers.IO) {
        val queryParts = mutableListOf("page=1", "perPage=$perPage", "skipTotal=1")
        if (!filter.isNullOrBlank()) {
            queryParts += "filter=${encodeParam(filter)}"
        }
        if (!sort.isNullOrBlank()) {
            queryParts += "sort=${encodeParam(sort)}"
        }
        val path = "/api/collections/$collection/records?${queryParts.joinToString("&")}"
        val resp = executeJson("GET", path, null, includeAuth = true)
        val items = resp.optJSONArray("items") ?: JSONArray()
        buildList(items.length()) {
            for (i in 0 until items.length()) {
                val obj = items.optJSONObject(i)
                if (obj != null) add(obj)
            }
        }
    }

    suspend fun getOneOrNull(
        collection: String,
        recordId: String,
    ): JSONObject? = withContext(Dispatchers.IO) {
        try {
            executeJson("GET", "/api/collections/$collection/records/$recordId", null, includeAuth = true)
        } catch (e: PocketBaseException) {
            if (e.statusCode == 404) null else throw e
        }
    }

    suspend fun createRecord(
        collection: String,
        payload: JSONObject,
    ): JSONObject = withContext(Dispatchers.IO) {
        val created = executeJson("POST", "/api/collections/$collection/records", payload, includeAuth = true)
        notifyCollectionChanged(collection)
        created
    }

    suspend fun createRecordMultipart(
        collection: String,
        fields: Map<String, String>,
        filePart: MultipartFilePart? = null,
    ): JSONObject = createRecordMultipartFiles(
        collection = collection,
        fields = fields,
        fileParts = listOfNotNull(filePart),
    )

    suspend fun createRecordMultipartFiles(
        collection: String,
        fields: Map<String, String>,
        fileParts: List<MultipartFilePart>,
    ): JSONObject = withContext(Dispatchers.IO) {
        val multipartBody = buildMultipartBody(fields, fileParts)
        val raw = executeRequestBody(
            method = "POST",
            path = "/api/collections/$collection/records",
            reqBody = multipartBody,
            includeAuth = true,
        )
        val created = if (raw.isBlank()) JSONObject() else JSONObject(raw)
        notifyCollectionChanged(collection)
        created
    }

    suspend fun updateRecord(
        collection: String,
        recordId: String,
        payload: JSONObject,
    ): JSONObject = withContext(Dispatchers.IO) {
        val updated = executeJson("PATCH", "/api/collections/$collection/records/$recordId", payload, includeAuth = true)
        syncUserSessionAfterPatch(collection, recordId, updated)
        notifyCollectionChanged(collection)
        updated
    }

    suspend fun updateRecordMultipart(
        collection: String,
        recordId: String,
        fields: Map<String, String>,
        filePart: MultipartFilePart? = null,
    ): JSONObject = withContext(Dispatchers.IO) {
        val multipartBody = buildMultipartBody(fields, listOfNotNull(filePart))
        val raw = executeRequestBody(
            method = "PATCH",
            path = "/api/collections/$collection/records/$recordId",
            reqBody = multipartBody,
            includeAuth = true,
        )
        val updated = if (raw.isBlank()) JSONObject() else JSONObject(raw)
        syncUserSessionAfterPatch(collection, recordId, updated)
        notifyCollectionChanged(collection)
        updated
    }

    fun buildFileUrl(
        collection: String,
        recordId: String,
        fileName: String,
        thumb: String? = null,
    ): String {
        val cleanId = recordId.trim()
        val cleanFile = fileName.trim()
        if (cleanId.isEmpty() || cleanFile.isEmpty()) return ""
        val encodedFile = encodeParam(cleanFile).replace("+", "%20")
        val base = "$baseUrl/api/files/$collection/$cleanId/$encodedFile"
        return if (!thumb.isNullOrBlank()) "$base?thumb=${encodeParam(thumb)}" else base
    }

    private suspend fun syncUserSessionAfterPatch(
        collection: String,
        recordId: String,
        updated: JSONObject,
    ) {
        if (collection == "users" && recordId == _sessionFlow.value?.userId) {
            val cur = _sessionFlow.value
            if (cur != null) {
                val nextSession = cur.copy(
                    displayName = updated.optString("name", cur.displayName).ifBlank { cur.displayName },
                    district = updated.optString("district", cur.district).ifBlank { cur.district },
                    isAdmin = updated.optBoolean("is_admin", cur.isAdmin) ||
                        cur.email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true),
                )
                saveSession(nextSession)
            }
        }
    }

    private fun buildMultipartBody(
        fields: Map<String, String>,
        fileParts: List<MultipartFilePart>,
    ): MultipartBody {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for ((key, value) in fields) {
            builder.addFormDataPart(key, value)
        }
        for (part in fileParts) {
            if (part.bytes.isNotEmpty()) {
                builder.addFormDataPart(
                    part.fieldName,
                    part.fileName,
                    part.bytes.toRequestBody(part.mimeType.toMediaType()),
                )
            }
        }
        return builder.build()
    }

    suspend fun deleteRecord(
        collection: String,
        recordId: String,
    ) = withContext(Dispatchers.IO) {
        executeRaw("DELETE", "/api/collections/$collection/records/$recordId", null, includeAuth = true)
        notifyCollectionChanged(collection)
    }

    fun notifyCollectionChanged(collection: String) {
        changeSignals.tryEmit(collection)
    }

    /**
     * Observes a PocketBase collection reactively:
     * 1. Emits immediately on subscription.
     * 2. Re-emits immediately whenever PocketBase /api/realtime SSE pushes an event for [collection]
     *    OR a local write occurs.
     * 3. Periodic lightweight sync fallback keeps two-way chat resilient even over unstable mobile carriers.
     */
    fun <T> observeCollection(
        collection: String,
        pollIntervalMs: Long = 6_000L,
        fetcher: suspend () -> T,
    ): Flow<T> = flow {
        emit(fetcher())
        kotlinx.coroutines.coroutineScope {
            val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
            val signalListener = launch {
                changeSignals.collect { changed ->
                    if (changed == "*" || changed == collection) {
                        trigger.tryEmit(Unit)
                    }
                }
            }
            val poller = launch {
                if (pollIntervalMs > 0L) {
                    while (isActive) {
                        delay(pollIntervalMs)
                        trigger.tryEmit(Unit)
                    }
                }
            }
            try {
                trigger.collect {
                    try {
                        emit(fetcher())
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        Log.w(TAG, "Reactive refresh warning on $collection: ${e.message}")
                    }
                }
            } finally {
                signalListener.cancel()
                poller.cancel()
            }
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    // --- POCKETBASE 0.22 REALTIME SSE (/api/realtime) ---

    fun startRealtimeStream() {
        sseJob?.cancel()
        sseJob = clientScope.launch {
            while (isActive) {
                val token = _sessionFlow.value?.token.orEmpty()
                if (token.isBlank()) {
                    delay(2_000L)
                    continue
                }
                try {
                    val req = Request.Builder()
                        .url("$baseUrl/api/realtime")
                        .header("Accept", "text/event-stream")
                        .header("Cache-Control", "no-cache")
                        .get()
                        .build()

                    sseClient.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            delay(4_000L)
                            return@use
                        }
                        val bodyStream = resp.body?.byteStream() ?: return@use
                        val reader = BufferedReader(InputStreamReader(bodyStream, Charsets.UTF_8))
                        var currentEvent = ""
                        val currentData = StringBuilder()

                        while (isActive) {
                            val line = reader.readLine() ?: break
                            when {
                                line.startsWith("event:") -> {
                                    currentEvent = line.removePrefix("event:").trim()
                                }
                                line.startsWith("data:") -> {
                                    currentData.append(line.removePrefix("data:").trim())
                                }
                                line.isEmpty() -> {
                                    if (currentEvent.isNotEmpty()) {
                                        handleSseFrame(currentEvent, currentData.toString())
                                    }
                                    currentEvent = ""
                                    currentData.setLength(0)
                                }
                            }
                        }
                    }
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    Log.d(TAG, "SSE reconnect scheduled: ${e.message}")
                    delay(3_500L)
                }
            }
        }
    }

    private suspend fun handleSseFrame(event: String, data: String) {
        if (event == "PB_CONNECT") {
            val clientId = try {
                JSONObject(data).optString("clientId")
            } catch (_: Exception) {
                ""
            }
            if (clientId.isNotBlank()) {
                subscribeRealtimeCollections(clientId)
            }
        } else {
            // Event format in PocketBase 0.22 is e.g. "chat_messages/*" or collection topic
            val collectionName = event.substringBefore("/")
            if (collectionName.isNotBlank()) {
                notifyCollectionChanged(collectionName)
            }
        }
    }

    private suspend fun subscribeRealtimeCollections(clientId: String) {
        try {
            val subs = JSONArray().apply {
                ALL_SUBSCRIPTIONS.forEach { put(it) }
            }
            val payload = JSONObject().apply {
                put("clientId", clientId)
                put("subscriptions", subs)
            }
            executeRaw("POST", "/api/realtime", payload, includeAuth = true)
        } catch (e: Exception) {
            Log.w(TAG, "Realtime subscription warning: ${e.message}")
        }
    }

    private fun Request.Builder.applyAuth(includeAuth: Boolean = true): Request.Builder {
        if (includeAuth) {
            val token = _sessionFlow.value?.token.orEmpty()
            if (token.isNotBlank()) {
                header("Authorization", token)
            }
        }
        return this
    }

    private fun executeJson(
        method: String,
        path: String,
        bodyJson: JSONObject?,
        includeAuth: Boolean,
    ): JSONObject {
        val raw = executeRaw(method, path, bodyJson, includeAuth)
        return if (raw.isBlank()) JSONObject() else JSONObject(raw)
    }

    private fun executeRaw(
        method: String,
        path: String,
        bodyJson: JSONObject?,
        includeAuth: Boolean,
    ): String {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val reqBody = bodyJson?.toString()?.toRequestBody(mediaType)
            ?: if (method == "POST" || method == "PATCH") "{}".toRequestBody(mediaType) else null
        return executeRequestBody(method, path, reqBody, includeAuth)
    }

    private fun executeRequestBody(
        method: String,
        path: String,
        reqBody: RequestBody?,
        includeAuth: Boolean,
    ): String {
        val req = Request.Builder()
            .url("$baseUrl$path")
            .applyAuth(includeAuth)
            .method(method, reqBody)
            .build()

        httpClient.newCall(req).execute().use { resp ->
            val respText = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val friendlyMsg = extractPocketBaseError(resp.code, respText)
                throw PocketBaseException(resp.code, friendlyMsg)
            }
            return respText
        }
    }

    private fun extractPocketBaseError(code: Int, rawBody: String): String {
        if (code == 404 && rawBody.contains("Missing collection", ignoreCase = true)) {
            return "Koleksi PocketBase belum di-import. Buka Settings → Import collections di Dashboard PocketBase Anda."
        }
        return try {
            val obj = JSONObject(rawBody)
            val dataObj = obj.optJSONObject("data")
            val detail = buildString {
                if (dataObj != null) {
                    val keys = dataObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val fieldMsg = dataObj.optJSONObject(k)?.optString("message").orEmpty()
                        if (fieldMsg.isNotBlank()) {
                            if (isNotEmpty()) append("; ")
                            append("$k: $fieldMsg")
                        }
                    }
                }
            }
            val baseMsg = obj.optString("message").ifBlank { "HTTP $code" }
            if (detail.isNotBlank()) "$baseMsg ($detail)" else baseMsg
        } catch (_: Exception) {
            "Gagal menghubungi PocketBase (HTTP $code)"
        }
    }

    companion object {
        private const val TAG = "PocketBaseClient"
        const val BOOTSTRAPPED_ADMIN_EMAIL = "nabilalailatus12@gmail.com"
        const val OAUTH2_REDIRECT_URI = "areksurabaya://oauth2redirect"

        private val KEY_BASE_URL = stringPreferencesKey("pb_base_url")
        private val KEY_TOKEN = stringPreferencesKey("pb_auth_token")
        private val KEY_USER_ID = stringPreferencesKey("pb_user_id")
        private val KEY_EMAIL = stringPreferencesKey("pb_user_email")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("pb_display_name")
        private val KEY_DISTRICT = stringPreferencesKey("pb_district")
        private val KEY_IS_ADMIN = booleanPreferencesKey("pb_is_admin")
        private val KEY_OAUTH_PROVIDER = stringPreferencesKey("pb_oauth_provider")
        private val KEY_OAUTH_VERIFIER = stringPreferencesKey("pb_oauth_verifier")
        private val KEY_OAUTH_STATE = stringPreferencesKey("pb_oauth_state")
        private val KEY_OAUTH_REDIRECT = stringPreferencesKey("pb_oauth_redirect")

        private val ALL_SUBSCRIPTIONS = listOf(
            "users/*",
            "bookmarks/*",
            "nearby_profiles/*",
            "incidents/*",
            "incident_comments/*",
            "events/*",
            "culinary/*",
            "culinary_reviews/*",
            "chat_rooms/*",
            "chat_messages/*",
        )

        @Volatile
        private var singletonInstance: PocketBaseClient? = null

        fun getInstance(context: Context): PocketBaseClient {
            return singletonInstance ?: synchronized(this) {
                singletonInstance ?: PocketBaseClient(
                    baseUrlInput = com.example.BuildConfig.POCKETBASE_URL.takeIf { it.isNotBlank() }
                        ?: context.applicationContext.getString(R.string.pocketbase_base_url),
                    dataStore = context.applicationContext.pbSessionDataStore,
                    enableRealtimeSse = true,
                ).also { singletonInstance = it }

            }
        }

        /**
         * PocketBase 0.22 requires custom record IDs to be strictly 15 lowercase alphanumeric characters (^[a-z0-9]{15}$).
         * SHA-256 hex lowercase first 15 chars satisfies this deterministically with zero collisions.
         */
        fun deterministicPbId(seed: String): String {
            val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray(Charsets.UTF_8))
            val hex = StringBuilder(32)
            for (i in 0 until 8) {
                hex.append(String.format("%02x", digest[i]))
            }
            return hex.substring(0, 15)
        }

        fun encodeParam(raw: String): String {
            return URLEncoder.encode(raw, "UTF-8")
        }
    }
}
