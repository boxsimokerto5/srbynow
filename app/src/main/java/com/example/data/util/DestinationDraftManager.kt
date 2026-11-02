package com.example.data.util

import android.content.Context
import com.example.data.model.DestinationDraft

object DestinationDraftManager {
    private const val PREFS_NAME = "surabaya_destination_draft_prefs"
    private const val KEY_TITLE = "draft_title"
    private const val KEY_CATEGORY = "draft_category"
    private const val KEY_DESCRIPTION = "draft_description"
    private const val KEY_ADDRESS = "draft_address"
    private const val KEY_DISTRICT = "draft_district"
    private const val KEY_PRICE = "draft_ticket_price"
    private const val KEY_HOURS = "draft_operational_hours"
    private const val KEY_BEST_TIME = "draft_best_time"
    private const val KEY_MAPS_URL = "draft_maps_url"
    private const val KEY_PHOTO_URIS = "draft_photo_uris"
    private const val KEY_SAVED_AT = "draft_saved_at"

    fun saveDraft(context: Context, draft: DestinationDraft) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TITLE, draft.title)
            .putString(KEY_CATEGORY, draft.category)
            .putString(KEY_DESCRIPTION, draft.description)
            .putString(KEY_ADDRESS, draft.address)
            .putString(KEY_DISTRICT, draft.district)
            .putString(KEY_PRICE, draft.ticketPrice)
            .putString(KEY_HOURS, draft.operationalHours)
            .putString(KEY_BEST_TIME, draft.bestTimeToVisit)
            .putString(KEY_MAPS_URL, draft.mapsUrl)
            .putString(KEY_PHOTO_URIS, draft.photoUris.joinToString("|||"))
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun loadDraft(context: Context): DestinationDraft? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString(KEY_TITLE, "").orEmpty()
        val desc = prefs.getString(KEY_DESCRIPTION, "").orEmpty()
        val addr = prefs.getString(KEY_ADDRESS, "").orEmpty()
        val mapsUrl = prefs.getString(KEY_MAPS_URL, "").orEmpty()
        val photoUrisRaw = prefs.getString(KEY_PHOTO_URIS, "").orEmpty()
        val photoUris = if (photoUrisRaw.isNotBlank()) {
            photoUrisRaw.split("|||").filter { it.isNotBlank() }
        } else {
            emptyList()
        }

        if (title.isBlank() && desc.isBlank() && addr.isBlank() && mapsUrl.isBlank() && photoUris.isEmpty()) {
            return null
        }

        return DestinationDraft(
            title = title,
            category = prefs.getString(KEY_CATEGORY, "Wisata Sejarah") ?: "Wisata Sejarah",
            description = desc,
            address = addr,
            district = prefs.getString(KEY_DISTRICT, "Genteng") ?: "Genteng",
            ticketPrice = prefs.getString(KEY_PRICE, "Gratis") ?: "Gratis",
            operationalHours = prefs.getString(KEY_HOURS, "08.00 - 17.00 WIB") ?: "08.00 - 17.00 WIB",
            bestTimeToVisit = prefs.getString(KEY_BEST_TIME, "Pagi / Sore Hari") ?: "Pagi / Sore Hari",
            mapsUrl = mapsUrl,
            photoUris = photoUris,
            savedAt = prefs.getLong(KEY_SAVED_AT, 0L),
        )
    }

    fun clearDraft(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
