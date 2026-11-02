package com.example.data.util

import android.content.Context
import com.example.data.model.EventDraft

object EventDraftManager {
    private const val PREFS_NAME = "surabaya_event_draft_prefs"
    private const val KEY_TITLE = "draft_event_title"
    private const val KEY_CATEGORY = "draft_event_category"
    private const val KEY_DATE_TEXT = "draft_event_date_text"
    private const val KEY_TIME_TEXT = "draft_event_time_text"
    private const val KEY_VENUE_NAME = "draft_event_venue_name"
    private const val KEY_DISTRICT = "draft_event_district"
    private const val KEY_ADDRESS = "draft_event_address"
    private const val KEY_PRICE = "draft_event_price"
    private const val KEY_DESCRIPTION = "draft_event_description"
    private const val KEY_MAPS_URL = "draft_event_maps_url"
    private const val KEY_PHOTO_URIS = "draft_event_photo_uris"
    private const val KEY_SAVED_AT = "draft_event_saved_at"

    fun saveDraft(context: Context, draft: EventDraft) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TITLE, draft.title)
            .putString(KEY_CATEGORY, draft.category)
            .putString(KEY_DATE_TEXT, draft.dateText)
            .putString(KEY_TIME_TEXT, draft.timeText)
            .putString(KEY_VENUE_NAME, draft.venueName)
            .putString(KEY_DISTRICT, draft.district)
            .putString(KEY_ADDRESS, draft.address)
            .putString(KEY_PRICE, draft.priceLabel)
            .putString(KEY_DESCRIPTION, draft.description)
            .putString(KEY_MAPS_URL, draft.mapsUrl)
            .putString(KEY_PHOTO_URIS, draft.photoUris.joinToString("|||"))
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun loadDraft(context: Context): EventDraft? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString(KEY_TITLE, "").orEmpty()
        val venue = prefs.getString(KEY_VENUE_NAME, "").orEmpty()
        val desc = prefs.getString(KEY_DESCRIPTION, "").orEmpty()
        val addr = prefs.getString(KEY_ADDRESS, "").orEmpty()
        val mapsUrl = prefs.getString(KEY_MAPS_URL, "").orEmpty()
        val photoUrisRaw = prefs.getString(KEY_PHOTO_URIS, "").orEmpty()
        val photoUris = if (photoUrisRaw.isNotBlank()) {
            photoUrisRaw.split("|||").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        if (title.isBlank() && venue.isBlank() && desc.isBlank() && addr.isBlank() && mapsUrl.isBlank() && photoUris.isEmpty()) {
            return null
        }

        return EventDraft(
            title = title,
            category = prefs.getString(KEY_CATEGORY, "FESTIVAL") ?: "FESTIVAL",
            dateText = prefs.getString(KEY_DATE_TEXT, "Minggu, 29 Nov 2026") ?: "Minggu, 29 Nov 2026",
            timeText = prefs.getString(KEY_TIME_TEXT, "18.30 - 21.30 WIB") ?: "18.30 - 21.30 WIB",
            venueName = venue,
            district = prefs.getString(KEY_DISTRICT, "Genteng") ?: "Genteng",
            address = addr,
            priceLabel = prefs.getString(KEY_PRICE, "Gratis") ?: "Gratis",
            description = desc,
            mapsUrl = mapsUrl,
            photoUris = photoUris,
            savedAt = prefs.getLong(KEY_SAVED_AT, System.currentTimeMillis()),
        )
    }

    fun clearDraft(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
