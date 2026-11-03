package com.example.ui.auth

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.isValidCitizenDisplayName
import com.example.data.pocketbase.PocketBaseClient
import com.example.ui.components.SurabayaDistrictDropdown
import com.example.ui.theme.FreshGreenGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OpenGreenBg
import com.example.ui.theme.OpenGreenText
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimaryDeep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun performSignOut(
    context: Context,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope,
) {
    val pb = PocketBaseClient.getInstance(context)
    scope.launch {
        try {
            pb.signOut()
        } finally {
            onSignOutComplete()
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pbClient = remember(context) { PocketBaseClient.getInstance(context) }

    var authTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Masuk, 1 = Daftar Akun Baru
    var fullName by rememberSaveable { mutableStateOf("") }
    var selectedDistrict by rememberSaveable { mutableStateOf("Genteng") }
    var selectedGender by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val oauthCallback by OAuthRedirectBus.pendingCallback.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val restored = pbClient.restorePersistedSession()
        if (restored != null) {
            onAuthSuccess()
        }
    }

    LaunchedEffect(oauthCallback) {
        val callback = oauthCallback ?: return@LaunchedEffect
        OAuthRedirectBus.consume()
        if (!callback.error.isNullOrBlank()) {
            errorMessage = "Login sosial dibatalkan: ${callback.error}"
            infoMessage = null
        } else if (!callback.code.isNullOrBlank()) {
            isLoading = true
            errorMessage = null
            infoMessage = "Menyelesaikan verifikasi akun sosial…"
            try {
                pbClient.completePendingOAuth2FromCallback(
                    code = callback.code,
                    returnedState = callback.state,
                )
                isLoading = false
                infoMessage = null
                onAuthSuccess()
            } catch (e: Exception) {
                isLoading = false
                infoMessage = null
                errorMessage = e.localizedMessage ?: "Gagal menyelesaikan login sosial."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF3898EC)),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_login_sky_sby_1793238473815),
            contentDescription = stringResource(R.string.app_name),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )

        // Subtle airy daylight scrim: keeps the upper blue sky & Tugu Pahlawan bright and vivid,
        // while gently cushioning the bottom white login card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x14084B8A),
                            Color(0x00FFFFFF),
                            Color(0x55E8F4FD),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top City Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.88f),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationCity,
                            contentDescription = null,
                            tint = Color(0xFF026AA7),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Warga Kota Pahlawan • Arek Suroboyo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0B3558),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Compact Auth Card (Pure crisp white)
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header Brand
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_mascot),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp)),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.tagline_surabaya),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    // Segmented Masuk / Daftar Pill
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEEF9F3),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            AuthModePill(
                                title = "Masuk Akun",
                                selected = authTab == 0,
                                onClick = {
                                    authTab = 0
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_login"),
                            )
                            AuthModePill(
                                title = "Daftar Warga",
                                selected = authTab == 1,
                                onClick = {
                                    authTab = 1
                                    errorMessage = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_register"),
                            )
                        }
                    }

                    if (authTab == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nama Lengkap Warga") },
                            placeholder = { Text("Contoh: Raka Pratama") },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                        )

                        SurabayaDistrictDropdown(
                            selectedDistrict = selectedDistrict,
                            onSelect = { selectedDistrict = it },
                            enabled = !isLoading,
                            label = "Kecamatan Domisili Surabaya (31 Kecamatan)",
                            testTag = "auth_district_dropdown",
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.mandatory_profile_gender_title),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilterChip(
                                    selected = selectedGender == "LAKI_LAKI",
                                    onClick = {
                                        if (!isLoading) {
                                            selectedGender = "LAKI_LAKI"
                                        }
                                    },
                                    label = { Text(stringResource(R.string.gender_option_male)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Male,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1565C0),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("auth_gender_male_chip"),
                                )
                                FilterChip(
                                    selected = selectedGender == "PEREMPUAN",
                                    onClick = {
                                        if (!isLoading) {
                                            selectedGender = "PEREMPUAN"
                                        }
                                    },
                                    label = { Text(stringResource(R.string.gender_option_female)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Female,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFAD1457),
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White,
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("auth_gender_female_chip"),
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Warga") },
                        placeholder = { Text("nama@email.com") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi (min. 6 karakter)") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan sandi" else "Lihat sandi",
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                    )

                    if (infoMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = OpenGreenBg,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = OpenGreenText,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = infoMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OpenGreenText,
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        }
                    }

                    val canSubmit = email.trim().contains("@") && password.length >= 6 &&
                        (authTab == 0 || (isValidCitizenDisplayName(fullName) && selectedGender.isNotBlank()))

                    Button(
                        onClick = {
                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            scope.launch {
                                try {
                                    if (authTab == 0) {
                                        pbClient.signInWithPassword(email.trim(), password)
                                    } else {
                                        pbClient.registerAndSignIn(
                                            name = fullName.trim(),
                                            email = email.trim(),
                                            password = password,
                                            district = selectedDistrict,
                                            gender = selectedGender,
                                        )
                                    }
                                    isLoading = false
                                    onAuthSuccess()
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = e.localizedMessage ?: "Gagal terhubung ke server PocketBase."
                                }
                            }
                        },
                        enabled = !isLoading && canSubmit,
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFFDCECE4),
                            contentColor = Color.White,
                            disabledContentColor = Color(0xFF6B8A7E),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("pocketbase_submit_auth_button"),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (!isLoading && canSubmit) {
                                        Modifier.background(FreshGreenGradient, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = ToskaPrimaryDeep,
                                        strokeWidth = 2.5.dp,
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = stringResource(R.string.signing_in_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = ToskaOnPrimaryContainer,
                                    )
                                } else {
                                    Text(
                                        text = if (authTab == 0) {
                                            stringResource(R.string.btn_google_sign_in)
                                        } else {
                                            stringResource(R.string.btn_register_account)
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }

                    CompactSocialLoginSection(
                        enabled = !isLoading,
                        onSelectProvider = { providerKey, providerLabel ->
                            errorMessage = null
                            infoMessage = null
                            scope.launch {
                                startPocketBaseSocialOAuth2(
                                    context = context,
                                    pbClient = pbClient,
                                    providerKey = providerKey,
                                    providerLabel = providerLabel,
                                    onInfo = { msg ->
                                        infoMessage = msg
                                        errorMessage = null
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                        infoMessage = null
                                    },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthModePill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(FreshGreenGradient, CircleShape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

