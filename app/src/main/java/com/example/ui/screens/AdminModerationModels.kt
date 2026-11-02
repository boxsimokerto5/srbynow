package com.example.ui.screens

import com.example.data.model.BarkasItem
import com.example.data.model.CitizenService
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentReport
import com.example.data.model.KosKontrakItem
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserMoment

data class AdminFlaggedItem(
    val id: String,
    val collection: String,
    val sourceLabel: String,
    val title: String,
    val snippet: String,
    val district: String,
    val authorId: String,
    val authorName: String,
    val contentReportCount: Int,
    val linkReportCount: Int,
    val hasExternalLink: Boolean,
    val createdAt: Long? = null,
) {
    val isQuarantined: Boolean
        get() = contentReportCount >= 10

    val isBlurred: Boolean
        get() = contentReportCount >= 5

    val isLinkFlagged: Boolean
        get() = linkReportCount >= 1

    val totalSignals: Int
        get() = contentReportCount + linkReportCount
}

fun collectAdminFlaggedItems(
    incidents: List<IncidentReport>,
    approvedEvents: List<SurabayaEvent>,
    pendingEvents: List<SurabayaEvent>,
    culinarySpots: List<CulinarySpot>,
    moments: List<UserMoment>,
    destinations: List<SurabayaDestination>,
    services: List<CitizenService>,
    barkasItems: List<BarkasItem> = emptyList(),
    kosListings: List<KosKontrakItem> = emptyList(),
): List<AdminFlaggedItem> {
    val aggregated = mutableListOf<AdminFlaggedItem>()

    incidents.filter { it.contentReportCount > 0 || it.linkReportCount > 0 }.forEach { item ->
        aggregated += AdminFlaggedItem(
            id = item.id,
            collection = "incidents",
            sourceLabel = "Laporan Kejadian",
            title = item.title,
            snippet = item.description.ifBlank { item.address },
            district = item.district,
            authorId = item.authorId,
            authorName = item.authorName.ifBlank { "Warga Surabaya" },
            contentReportCount = item.contentReportCount,
            linkReportCount = item.linkReportCount,
            hasExternalLink = item.mapsUrl.isNotBlank(),
            createdAt = item.createdAt,
        )
    }

    (approvedEvents + pendingEvents).distinctBy { it.id }
        .filter { it.contentReportCount > 0 || it.linkReportCount > 0 }
        .forEach { ev ->
            aggregated += AdminFlaggedItem(
                id = ev.id,
                collection = "events",
                sourceLabel = "Agenda Event",
                title = ev.title,
                snippet = "${ev.eventDateText} • ${ev.venueName}".trim(' ', '•'),
                district = ev.district,
                authorId = ev.proposerId,
                authorName = ev.proposerName.ifBlank { "Pengusul Event" },
                contentReportCount = ev.contentReportCount,
                linkReportCount = ev.linkReportCount,
                hasExternalLink = ev.mapsUrl.isNotBlank(),
                createdAt = ev.createdAt,
            )
        }

    culinarySpots.filter { it.contentReportCount > 0 || it.linkReportCount > 0 }.forEach { spot ->
        aggregated += AdminFlaggedItem(
            id = spot.id,
            collection = "culinary",
            sourceLabel = "Direktori Kuliner",
            title = spot.name,
            snippet = spot.description.ifBlank { spot.address },
            district = spot.district,
            authorId = spot.createdBy,
            authorName = spot.authorName.ifBlank { "Kontributor Kuliner" },
            contentReportCount = spot.contentReportCount,
            linkReportCount = spot.linkReportCount,
            hasExternalLink = spot.hasAnyExternalLink,
            createdAt = spot.createdAt,
        )
    }

    moments.filter { it.contentReportCount > 0 }.forEach { m ->
        aggregated += AdminFlaggedItem(
            id = m.id,
            collection = "moments",
            sourceLabel = if (m.isLensaWarga) "Lensa Warga" else "Momen Warga",
            title = m.locationName.ifBlank { "Kiriman ${m.authorName}" },
            snippet = m.caption,
            district = m.district,
            authorId = m.authorId,
            authorName = m.authorName.ifBlank { "Warga Surabaya" },
            contentReportCount = m.contentReportCount,
            linkReportCount = 0,
            hasExternalLink = false,
            createdAt = m.createdAt,
        )
    }

    destinations.filter { it.contentReportCount > 0 || it.linkReportCount > 0 }.forEach { dest ->
        aggregated += AdminFlaggedItem(
            id = dest.id,
            collection = "destinations",
            sourceLabel = "Destinasi Wisata",
            title = dest.title,
            snippet = dest.description,
            district = dest.district,
            authorId = dest.createdBy,
            authorName = dest.authorName,
            contentReportCount = dest.contentReportCount,
            linkReportCount = dest.linkReportCount,
            hasExternalLink = dest.mapsUrl.isNotBlank(),
            createdAt = dest.createdAt,
        )
    }

    services.filter { it.contentReportCount > 0 }.forEach { svc ->
        aggregated += AdminFlaggedItem(
            id = svc.id,
            collection = "services",
            sourceLabel = "Jasa Warga",
            title = svc.title,
            snippet = "${svc.priceDisplay} • ${svc.description}",
            district = svc.district,
            authorId = svc.providerId,
            authorName = svc.providerName.ifBlank { "Penyedia Jasa" },
            contentReportCount = svc.contentReportCount,
            linkReportCount = 0,
            hasExternalLink = false,
            createdAt = svc.createdAt,
        )
    }

    barkasItems.filter { it.contentReportCount > 0 }.forEach { brk ->
        aggregated += AdminFlaggedItem(
            id = brk.id,
            collection = "barkas_items",
            sourceLabel = "Barkas Warga",
            title = brk.title,
            snippet = "${brk.formattedPrice} (${brk.negotiableLabel}) • ${brk.description}",
            district = brk.district,
            authorId = brk.sellerId,
            authorName = brk.sellerName.ifBlank { "Penjual Barkas" },
            contentReportCount = brk.contentReportCount,
            linkReportCount = 0,
            hasExternalLink = false,
            createdAt = brk.createdAt,
        )
    }

    kosListings.filter { it.contentReportCount > 0 || it.linkReportCount > 0 }.forEach { kos ->
        aggregated += AdminFlaggedItem(
            id = kos.id,
            collection = "kos_listings",
            sourceLabel = "Info Kos & Kontrak",
            title = kos.title,
            snippet = "${kos.priceWithPeriod} • Kec. ${kos.district} (${kos.address})",
            district = kos.district,
            authorId = kos.ownerId,
            authorName = kos.ownerName.ifBlank { "Pemilik Kos" },
            contentReportCount = kos.contentReportCount,
            linkReportCount = kos.linkReportCount,
            hasExternalLink = kos.hasAnyExternalLink,
            createdAt = kos.createdAt,
        )
    }

    return aggregated.sortedWith(
        compareByDescending<AdminFlaggedItem> { it.isQuarantined }
            .thenByDescending { it.totalSignals }
            .thenByDescending { it.createdAt ?: 0L },
    )
}
