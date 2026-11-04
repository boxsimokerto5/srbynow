package com.example.data.seed

import com.example.data.model.CitizenPoll
import com.example.data.model.EventComment
import com.example.data.model.PollComment
import com.example.data.model.TambalBanComment
import com.example.data.model.TambalBanSpot
import org.json.JSONObject

object SurabayaCuratedSeed {
    fun starterIncidents(uid: String = "", authorName: String = ""): List<JSONObject> = emptyList()
    fun starterCulinary(uid: String = ""): List<JSONObject> = emptyList()
    fun starterPendingEvents(uid: String = ""): List<JSONObject> = emptyList()
    fun starterApprovedEvents(uid: String = ""): List<JSONObject> = emptyList()
    fun starterPublicRooms(uid: String = ""): List<JSONObject> = emptyList()
    fun starterCommunityPeers(uid: String = ""): List<JSONObject> = emptyList()
    fun starterMoments(uid: String = "", authorName: String = ""): List<JSONObject> = emptyList()
    fun starterDestinations(adminUid: String = ""): List<JSONObject> = emptyList()
    fun starterServices(uid: String = "", authorName: String = ""): List<JSONObject> = emptyList()
    fun starterPolls(uid: String = ""): List<CitizenPoll> = emptyList()
    fun starterPollComments(): Map<String, List<PollComment>> = emptyMap()
    fun starterTambalBanSpots(uid: String = ""): List<TambalBanSpot> = emptyList()
    fun starterTambalBanComments(): Map<String, List<TambalBanComment>> = emptyMap()
    fun starterEventComments(): Map<String, List<EventComment>> = emptyMap()
}
