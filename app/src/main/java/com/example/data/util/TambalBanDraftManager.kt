package com.example.data.util

import android.content.Context
import com.example.data.model.TambalBanDraft

object TambalBanDraftManager {
    private const val PREFS_NAME = "surabaya_tambal_ban_draft_prefs"
    private const val KEY_NAME = "draft_name"
    private const val KEY_DESCRIPTION = "draft_description"
    private const val KEY_DISTRICT = "draft_district"
    private const val KEY_ADDRESS = "draft_address"
    private const val KEY_MAPS_URL = "draft_maps_url"
    private const val KEY_PHONE = "draft_phone"
    private const val KEY_WHATSAPP = "draft_whatsapp"
    private const val KEY_IS_24_HOURS = "draft_is_24_hours"
    private const val KEY_IS_TUBELESS = "draft_is_tubeless"
    private const val KEY_FOR_MOTOR = "draft_for_motor"
    private const val KEY_FOR_MOBIL = "draft_for_mobil"
    private const val KEY_CAN_CALL_OUT = "draft_can_call_out"
    private const val KEY_PHOTO_URI = "draft_photo_uri"
    private const val KEY_SAVED_AT = "draft_saved_at"

    fun saveDraft(context: Context, draft: TambalBanDraft) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, draft.name)
            .putString(KEY_DESCRIPTION, draft.description)
            .putString(KEY_DISTRICT, draft.district)
            .putString(KEY_ADDRESS, draft.address)
            .putString(KEY_MAPS_URL, draft.mapsUrl)
            .putString(KEY_PHONE, draft.phoneNumber)
            .putString(KEY_WHATSAPP, draft.whatsappNumber)
            .putBoolean(KEY_IS_24_HOURS, draft.is24Hours)
            .putBoolean(KEY_IS_TUBELESS, draft.isTubeless)
            .putBoolean(KEY_FOR_MOTOR, draft.forMotor)
            .putBoolean(KEY_FOR_MOBIL, draft.forMobil)
            .putBoolean(KEY_CAN_CALL_OUT, draft.canCallOut)
            .putString(KEY_PHOTO_URI, draft.photoUri)
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun loadDraft(context: Context): TambalBanDraft? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, "").orEmpty()
        val desc = prefs.getString(KEY_DESCRIPTION, "").orEmpty()
        val addr = prefs.getString(KEY_ADDRESS, "").orEmpty()
        val mapsUrl = prefs.getString(KEY_MAPS_URL, "").orEmpty()
        val phone = prefs.getString(KEY_PHONE, "").orEmpty()
        val wa = prefs.getString(KEY_WHATSAPP, "").orEmpty()
        val photoUri = prefs.getString(KEY_PHOTO_URI, "").orEmpty()

        if (name.isBlank() && desc.isBlank() && addr.isBlank() && mapsUrl.isBlank() && phone.isBlank() && wa.isBlank() && photoUri.isBlank()) {
            return null
        }

        return TambalBanDraft(
            name = name,
            description = desc,
            district = prefs.getString(KEY_DISTRICT, "Genteng") ?: "Genteng",
            address = addr,
            mapsUrl = mapsUrl,
            phoneNumber = phone,
            whatsappNumber = wa,
            is24Hours = prefs.getBoolean(KEY_IS_24_HOURS, false),
            isTubeless = prefs.getBoolean(KEY_IS_TUBELESS, true),
            forMotor = prefs.getBoolean(KEY_FOR_MOTOR, true),
            forMobil = prefs.getBoolean(KEY_FOR_MOBIL, false),
            canCallOut = prefs.getBoolean(KEY_CAN_CALL_OUT, false),
            photoUri = photoUri,
            savedAt = prefs.getLong(KEY_SAVED_AT, System.currentTimeMillis()),
        )
    }

    fun clearDraft(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
