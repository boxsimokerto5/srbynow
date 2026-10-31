package com.example.data.seed

import com.example.data.model.CitizenPoll
import com.example.data.model.PollComment
import com.example.data.model.PollOption
import com.example.data.model.TambalBanComment
import com.example.data.model.TambalBanSpot
import com.example.data.pocketbase.PocketBaseClient
import org.json.JSONObject

object SurabayaCuratedSeed {

    fun starterIncidents(uid: String, authorName: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_inc_ahmad_yani"))
            put("author_id", uid)
            put("author_name", authorName)
            put("title", "Arus Padat Merayap Jl. Ahmad Yani Arah Wonokromo")
            put("description", "Volume kendaraan meningkat menjelang jam pulang kerja di depan Royal Plaza hingga flyover Wonokromo. Petugas Dishub Surabaya sudah mengatur lajur frontage barat.")
            put("category", "LALU_LINTAS")
            put("severity", "PENTING")
            put("district", "Wonokromo")
            put("address", "Jl. Ahmad Yani No. 16-18, Wonokromo, Surabaya")
            put("lat", -7.3092)
            put("lng", 112.7347)
            put("image_key", "kejadian_kota")
            put("verified_count", 14)
            put("verified_by_uids", uid)
            put("comment_count", 1)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_inc_tunjungan"))
            put("author_id", uid)
            put("author_name", "Redaksi Warga SBY")
            put("title", "Penataan Lampu Heritage & Jalur Pejalan Kaki Tunjungan")
            put("description", "Area trotoar sisi timur Jl. Tunjungan sedang ada perawatan lampu taman oleh petugas DKRTH. Pejalan kaki tetap nyaman melintas dan spot foto tetap buka.")
            put("category", "FASILITAS")
            put("severity", "INFO")
            put("district", "Genteng")
            put("address", "Jl. Tunjungan No. 45, Genteng, Surabaya")
            put("lat", -7.2591)
            put("lng", 112.7385)
            put("image_key", "hero_surabaya")
            put("verified_count", 9)
            put("verified_by_uids", "")
            put("comment_count", 0)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_inc_cuaca_barat"))
            put("author_id", uid)
            put("author_name", "Info Cuaca Suroboyo")
            put("title", "Hujan Ringan Disertai Angin di Kawasan Wiyung & Darmo")
            put("description", "Pengendara roda dua diimbau berhati-hati dan tidak berteduh di bawah pohon besar maupun jembatan penyeberangan. Pompa air kota terpantau aktif normal.")
            put("category", "CUACA_BANJIR")
            put("severity", "DARURAT")
            put("district", "Wiyung")
            put("address", "Jl. Raya Menganti Wiyung, Surabaya Barat")
            put("lat", -7.3125)
            put("lng", 112.6980)
            put("image_key", "kejadian_kota")
            put("verified_count", 21)
            put("verified_by_uids", "")
            put("comment_count", 0)
        },
    )

    fun starterCulinary(uid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_kul_rawon_embong"))
            put("created_by", uid)
            put("author_name", "Kurator Kuliner SBY")
            put("name", "Rawon Setan Embong Malang")
            put("category", "RAWON_SOTO")
            put("description", "Kuah kluwek hitam pekat gurih dengan potongan daging sapi empuk berukuran besar, disajikan hangat bersama kecambah segar, telur asin, dan sambal terasi pedas.")
            put("district", "Genteng")
            put("address", "Jl. Embong Malang No. 78/I, Genteng, Surabaya")
            put("price_range", "Rp 38.000 - Rp 50.000")
            put("open_hour", 8)
            put("close_hour", 23)
            put("lat", -7.2586)
            put("lng", 112.7354)
            put("image_key", "kuliner_rawon")
            put("rating_avg", 4.9)
            put("review_count", 3)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_kul_sego_sambel"))
            put("created_by", uid)
            put("author_name", "Kurator Kuliner SBY")
            put("name", "Sego Sambel Mak Yeye")
            put("category", "PENYETAN_SAMBAL")
            put("description", "Ikon kuliner malam Wonokromo sejak 1982. Ikan pari (iwak pe) hangat, tempe goreng, dan telur dadar disiram sambal tomat manis-pedas segar.")
            put("district", "Wonokromo")
            put("address", "Jl. Jagir Wonokromo Wetan No. 10, Surabaya")
            put("price_range", "Rp 20.000 - Rp 30.000")
            put("open_hour", 16)
            put("close_hour", 24)
            put("lat", -7.3008)
            put("lng", 112.7420)
            put("image_key", "kuliner_rawon")
            put("rating_avg", 4.8)
            put("review_count", 2)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_kul_tahu_tek"))
            put("created_by", uid)
            put("author_name", "Kurator Kuliner SBY")
            put("name", "Tahu Tek Pak Jayen Dharmahusada")
            put("category", "LEGENDARIS")
            put("description", "Bumbu petis udang khas Surabaya yang kental dan medok, berpadu tahu goreng setengah matang, lontong lembut, kentang, dan kerupuk udang renyah.")
            put("district", "Gubeng")
            put("address", "Jl. Dharmahusada Indah I Blok C-2, Gubeng, Surabaya")
            put("price_range", "Rp 18.000 - Rp 25.000")
            put("open_hour", 17)
            put("close_hour", 23)
            put("lat", -7.2698)
            put("lng", 112.7689)
            put("image_key", "kuliner_rawon")
            put("rating_avg", 4.8)
            put("review_count", 2)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_kul_kopi_tunjungan"))
            put("created_by", uid)
            put("author_name", "Kurator Kuliner SBY")
            put("name", "Kopi Romansa Tunjungan")
            put("category", "KOPI_NONGKRONG")
            put("description", "Kedai kopi berkonsep bangunan kolonial modern dengan pemandangan langsung lampu jalan Tunjungan. Favorit es kopi susu gula aren & croissant pandan.")
            put("district", "Genteng")
            put("address", "Jl. Tunjungan No. 82, Genteng, Surabaya")
            put("price_range", "Rp 22.000 - Rp 42.000")
            put("open_hour", 7)
            put("close_hour", 23)
            put("lat", -7.2572)
            put("lng", 112.7379)
            put("image_key", "hero_surabaya")
            put("rating_avg", 4.7)
            put("review_count", 2)
        },
    )

    fun starterPendingEvents(uid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_ev_pending_kota_lama"))
            put("proposer_id", uid)
            put("proposer_name", "Komunitas Sketsa SBY")
            put("title", "Jelajah Sore & Sketsa Bareng Kota Lama Surabaya")
            put("category", "KOMUNITAS")
            put("event_date_text", "Minggu, 22 Nov 2026")
            put("time_text", "15.30 - 18.00 WIB")
            put("venue_name", "Plaza Internatio Kota Lama")
            put("district", "Pabean Cantian")
            put("address", "Jl. Rajawali, Krembangan, Surabaya Utara")
            put("lat", -7.2348)
            put("lng", 112.7365)
            put("price_label", "Gratis")
            put("description", "Menggambar suasana arsitektur bersejarah kawasan Eropa Kota Lama Surabaya terbuka untuk pemula maupun ilustrator. Bawa alat gambar masing-masing.")
            put("image_key", "hero_surabaya")
            put("status", "PENDING")
            put("admin_note", "")
            put("interested_count", 0)
            put("interested_uids", "")
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_ev_pending_kenjeran"))
            put("proposer_id", uid)
            put("proposer_name", "Suroboyo Runners")
            put("title", "Sunrise Fun Run 5K Jembatan Suroboyo Kenjeran")
            put("category", "OLAHRAGA")
            put("event_date_text", "Sabtu, 28 Nov 2026")
            put("time_text", "05.30 - 08.00 WIB")
            put("venue_name", "Taman Suroboyo Kenjeran")
            put("district", "Kenjeran")
            put("address", "Jl. Pantai Kenjeran, Bulak, Surabaya")
            put("lat", -7.2321)
            put("lng", 112.7945)
            put("price_label", "Gratis")
            put("description", "Lari pagi santai menikmati matahari terbit di pesisir timur Surabaya dilanjutkan sarapan lontong kupang bersama komunitas.")
            put("image_key", "event_festival")
            put("status", "PENDING")
            put("admin_note", "")
            put("interested_count", 0)
            put("interested_uids", "")
        },
    )

    fun starterApprovedEvents(uid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_ev_approved_bungkul"))
            put("proposer_id", uid)
            put("proposer_name", "Disbudporapar & UMKM SBY")
            put("title", "Festival Malam Akustik & UMKM Kreatif Taman Bungkul")
            put("category", "FESTIVAL")
            put("event_date_text", "Sabtu, 14 Nov 2026")
            put("time_text", "18.30 - 22.00 WIB")
            put("venue_name", "Amphitheater Taman Bungkul")
            put("district", "Wonokromo")
            put("address", "Jl. Taman Bungkul, Darmo, Wonokromo, Surabaya")
            put("lat", -7.2913)
            put("lng", 112.7394)
            put("price_label", "Gratis")
            put("description", "Panggung musik akustik musisi muda Surabaya, bazar 40+ produk kerajinan & jajanan lokal, serta pojok ramah keluarga di jantung kota.")
            put("image_key", "event_festival")
            put("status", "APPROVED")
            put("admin_note", "Terverifikasi resmi untuk kalender akhir pekan warga.")
            put("interested_count", 48)
            put("interested_uids", uid)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_ev_approved_tunjungan"))
            put("proposer_id", uid)
            put("proposer_name", "Suroboyo Creative Hub")
            put("title", "Pasar Seni & Kriya Koridor Siola Tunjungan")
            put("category", "MUSIK_SENI")
            put("event_date_text", "Jumat - Minggu, 20-22 Nov 2026")
            put("time_text", "16.00 - 21.30 WIB")
            put("venue_name", "Koridor Co-Working Siola")
            put("district", "Genteng")
            put("address", "Jl. Tunjungan No. 1, Genteng, Surabaya")
            put("lat", -7.2561)
            put("lng", 112.7376)
            put("price_label", "Gratis")
            put("description", "Pameran ilustrasi bertema Kota Pahlawan, live Sablon Cukil, diskusi desain urban, dan pertunjukan keroncong modern.")
            put("image_key", "hero_surabaya")
            put("status", "APPROVED")
            put("admin_note", "Disetujui Admin Surabaya Hari Ini.")
            put("interested_count", 31)
            put("interested_uids", "")
        },
    )

    fun starterPublicRooms(uid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            val roomKey = "public_ruang_warga"
            put("id", PocketBaseClient.deterministicPbId(roomKey))
            put("room_key", roomKey)
            put("room_type", "PUBLIC")
            put("title", "Ruang Warga Surabaya")
            put("subtitle", "Obrolan santai & kabar harian arek Suroboyo")
            put("participant_uid_1", "")
            put("participant_uid_2", "")
            put("participant_names", "")
            put("last_message", "Sugeng rawuh rek! Yuk berbagi info terkini seputar Surabaya.")
            put("last_sender_name", "Komunitas SBY")
            put("created_by", uid)
        },
        JSONObject().apply {
            val roomKey = "public_lalin_cuaca"
            put("id", PocketBaseClient.deterministicPbId(roomKey))
            put("room_key", roomKey)
            put("room_type", "PUBLIC")
            put("title", "Pantauan Jalan & Cuaca SBY")
            put("subtitle", "Info cepat macet, hujan, dan rute alternatif")
            put("participant_uid_1", "")
            put("participant_uid_2", "")
            put("participant_names", "")
            put("last_message", "Jalur Merr arah Rungkut terpantau lancar sore ini.")
            put("last_sender_name", "Info Lalin")
            put("created_by", uid)
        },
        JSONObject().apply {
            val roomKey = "public_kuliner_malam"
            put("id", PocketBaseClient.deterministicPbId(roomKey))
            put("room_key", roomKey)
            put("room_type", "PUBLIC")
            put("title", "Grup Kulineran & Ngopi SBY")
            put("subtitle", "Rekomendasi warung legendaris & spot nongkrong baru")
            put("participant_uid_1", "")
            put("participant_uid_2", "")
            put("participant_names", "")
            put("last_message", "Ada rekomendasi penyetan enak daerah Gubeng malam ini?")
            put("last_sender_name", "Pecinta Sambal")
            put("created_by", uid)
        },
    )

    fun starterCommunityPeers(uid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("peer_bima_gubeng"))
            put("user_id", uid)
            put("display_name", "Bima Pratama")
            put("avatar_color_hex", "#00798C")
            put("bio", "Suka hunting kopi pagi di Tunjungan & fotografi jalanan Surabaya.")
            put("district", "Gubeng")
            put("interests", "Fotografi, Kopi, Lari Pagi")
            put("lat", -7.2725)
            put("lng", 112.7550)
            put("is_discoverable", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("peer_nadia_wonokromo"))
            put("user_id", uid)
            put("display_name", "Nadia Kusuma")
            put("avatar_color_hex", "#C86D1F")
            put("bio", "Penggemar kuliner pedas Suroboyo & aktif di acara Taman Bungkul.")
            put("district", "Wonokromo")
            put("interests", "Kuliner Pedas, Musik Akustik, UMKM")
            put("lat", -7.2940)
            put("lng", 112.7390)
            put("is_discoverable", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("peer_reza_rungkut"))
            put("user_id", uid)
            put("display_name", "Reza Mahendra")
            put("avatar_color_hex", "#006B5E")
            put("bio", "Anak IT daerah Rungkut, sering gowes akhir pekan ke Kenjeran.")
            put("district", "Rungkut")
            put("interests", "Sepeda, Teknologi, Tahu Tek")
            put("lat", -7.3195)
            put("lng", 112.7810)
            put("is_discoverable", true)
        },
    )

    fun starterMoments(uid: String, authorName: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_moment_tunjungan"))
            put("author_id", uid)
            put("author_name", authorName)
            put("author_district", "Genteng")
            put("caption", "Mlaku-mlaku nang Tunjungan pas malem minggu. Lampu heritage-e cakep pol, hawane syahdu rek!")
            put("location_name", "Jl. Tunjungan")
            put("district", "Genteng")
            put("image_key", "hero_surabaya")
            put("likes_count", 38)
            put("liked_by_uids", uid)
            put("comment_count", 2)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_moment_kenjeran"))
            put("author_id", uid)
            put("author_name", "Bima Prasetyo")
            put("author_district", "Kenjeran")
            put("caption", "Gowes subuh tekan Jembatan Suroboyo. View sunrise Selat Madura pancen gak tau ngecewano.")
            put("location_name", "Jembatan Suroboyo")
            put("district", "Kenjeran")
            put("image_key", "hero_surabaya")
            put("likes_count", 24)
            put("liked_by_uids", "")
            put("comment_count", 1)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_moment_bungkul"))
            put("author_id", uid)
            put("author_name", "Nadia Permata")
            put("author_district", "Wonokromo")
            put("caption", "Santai sore nang Taman Bungkul karo mangan rawon kalkulator. Arek Suroboyo pasti paham nikmate!")
            put("location_name", "Taman Bungkul")
            put("district", "Wonokromo")
            put("image_key", "kuliner_rawon")
            put("likes_count", 45)
            put("liked_by_uids", "")
            put("comment_count", 3)
        },
    )

    fun starterDestinations(adminUid: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_kota_lama"))
            put("created_by", adminUid)
            put("author_name", "Dinas Pariwisata SBY")
            put("title", "Kawasan Wisata Kota Lama Surabaya")
            put("category", "WISATA_SEJARAH")
            put("description", "Ikon wisata sejarah terbaru Surabaya yang terbagi atas Zona Eropa, Zona Pecinan, dan Zona Arab. Dilengkapi bangunan kolonial megah, penyewaan sepeda kuno, dan tur mobil jeep heritage.")
            put("district", "Pabean Cantian")
            put("address", "Jl. Rajawali & Jl. Karet, Pabean Cantian, Surabaya")
            put("ticket_price", "Gratis")
            put("operational_hours", "24 Jam (Terbaik 16.00 - 22.00 WIB)")
            put("best_time", "Sore menjelang malam hari")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/surabayakotalama")
            put("rating_avg", 4.9)
            put("review_count", 86)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_monkasel"))
            put("created_by", adminUid)
            put("author_name", "Admin Resmi Surabaya")
            put("title", "Monumen Kapal Selam (Monkasel)")
            put("category", "MUSEUM")
            put("description", "Museum maritim unik di pusat kota berupa kapal selam asli KRI Pasopati 410 buatan Uni Soviet yang pernah bertugas dalam Operasi Trikora. Pengunjung dapat masuk ke dalam ruang torpedo dan periskop.")
            put("district", "Genteng")
            put("address", "Jl. Pemuda No. 39, Embong Kaliasin, Genteng, Surabaya")
            put("ticket_price", "Rp 15.000 / orang")
            put("operational_hours", "08.00 - 21.00 WIB")
            put("best_time", "Pagi atau siang hari")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/monkaselsby")
            put("rating_avg", 4.8)
            put("review_count", 52)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_alun_alun"))
            put("created_by", adminUid)
            put("author_name", "Pemerintah Kota Surabaya")
            put("title", "Alun-Alun Surabaya & Galeri Seni Basement")
            put("category", "WISATA_SEJARAH")
            put("description", "Alun-alun modern dengan kompleks Balai Pemuda heritage di atasnya dan ruang galeri seni bawah tanah (basement) ber-AC yang estetik untuk pameran lukisan, foto sejarah, serta arena skateboard.")
            put("district", "Genteng")
            put("address", "Jl. Gubernur Suryo No. 15, Embong Kaliasin, Genteng, Surabaya")
            put("ticket_price", "Gratis")
            put("operational_hours", "07.00 - 21.00 WIB")
            put("best_time", "Sore hari")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/alunalunsby")
            put("rating_avg", 4.9)
            put("review_count", 110)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_bungkul"))
            put("created_by", adminUid)
            put("author_name", "Admin Resmi Surabaya")
            put("title", "Taman Bungkul Surabaya")
            put("category", "TAMAN_KOTA")
            put("description", "Taman kota legendaris peraih penghargaan The 2013 Asian Townscape Award PBB. Dilengkapi jogging track rindang, area skateboard, panggung budaya, wifi gratis, dan sentra kuliner PKL terpadu.")
            put("district", "Wonokromo")
            put("address", "Jl. Raya Darmo, Darmo, Wonokromo, Surabaya")
            put("ticket_price", "Gratis")
            put("operational_hours", "24 Jam")
            put("best_time", "Pagi & Malam hari")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/tamanbungkulsby")
            put("rating_avg", 4.9)
            put("review_count", 240)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_romokalisari"))
            put("created_by", adminUid)
            put("author_name", "Dinas Pariwisata SBY")
            put("title", "Romokalisari Adventure Land")
            put("category", "WISATA_BAHARI")
            put("description", "Wahana petualangan alam pesisir Surabaya Barat dengan berbagai atraksi seru: naik kano menyusuri hutan mangrove, jetski, ATV, perahu bebek, mini zoo, dan sentra ikan bakar khas nelayan.")
            put("district", "Benowo")
            put("address", "Kawasan Romokalisari, Benowo, Surabaya Barat")
            put("ticket_price", "Rp 10.000 (Wahana Rp 10k-50k)")
            put("operational_hours", "08.00 - 17.00 WIB")
            put("best_time", "Pagi atau sore hari")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/romokalisari")
            put("rating_avg", 4.7)
            put("review_count", 43)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_dest_perahu_kalimas"))
            put("created_by", adminUid)
            put("author_name", "Admin Resmi Surabaya")
            put("title", "Wisata Perahu Kalimas Surabaya")
            put("category", "WISATA_BAHARI")
            put("description", "Susur sungai Kalimas Surabaya di malam hari dengan gemerlap lampion dan lampu warna-warni dari dermaga Monkasel melintasi Taman Prestasi hingga Museum Pendidikan.")
            put("district", "Genteng")
            put("address", "Dermaga Monkasel / Taman Prestasi, Jl. Ketabang Kali, Surabaya")
            put("ticket_price", "Rp 4.000 - Rp 10.000")
            put("operational_hours", "15.00 - 21.00 WIB")
            put("best_time", "Malam hari (18.30 - 21.00)")
            put("image_key", "hero_surabaya")
            put("maps_url", "https://maps.app.goo.gl/perahukalimas")
            put("rating_avg", 4.8)
            put("review_count", 78)
        },
    )

    fun starterServices(uid: String, authorName: String): List<JSONObject> = listOf(
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_srv_cuci_ac_abdulah"))
            put("provider_id", uid)
            put("provider_name", "Cak Abdulah Service AC")
            put("title", "Cuci AC Dingin Maknyus & Tambah Freon Abdulah")
            put("category", "ELEKTRONIK_AC")
            put("price", 75000L)
            put("price_unit", "unit")
            put("district", "Gubeng")
            put("coverage_area", "Gubeng, Sukolilo, Rungkut, Tambaksari & Seluruh Surabaya")
            put("phone_number", "081234567890")
            put("whatsapp_number", "6281234567890")
            put("description", "Melayani cuci AC split (1/2 PK - 2 PK), tambah freon R32 / R410, perbaikan tetesan air & mati total. Teknisi amanah, rapi, ramah dan bergaransi 30 hari. Tidak minta DP, bayar setelah selesai diperiksa.")
            put("rating_avg", 4.9)
            put("review_count", 38)
            put("verified_badge", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_srv_ledeng_sugeng"))
            put("provider_id", uid)
            put("provider_name", "Sugeng Plumbing Surabaya")
            put("title", "Tukang Ledeng & Saluran Mampet Tanpa Bongkar")
            put("category", "PERBAIKAN_RUMAH")
            put("price", 120000L)
            put("price_unit", "titik")
            put("district", "Wonokromo")
            put("coverage_area", "Wonokromo, Sawahan, Tegalsari & Surabaya Selatan")
            put("phone_number", "081398765432")
            put("whatsapp_number", "6281398765432")
            put("description", "Ahli instalasi pipa paralon bocor, kran patah di dinding, tandon air kotor, wastafel mampet pakai mesin spiral tanpa merusak lantai keramik. Siap darurat 24 jam.")
            put("rating_avg", 4.8)
            put("review_count", 24)
            put("verified_badge", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_srv_home_clean_sri"))
            put("provider_id", uid)
            put("provider_name", "Mbak Sri Home Care Surabaya")
            put("title", "Jasa Bersih Rumah, Kos & Kantor Arek Resik")
            put("category", "KEBERSIHAN")
            put("price", 50000L)
            put("price_unit", "jam")
            put("district", "Tegalsari")
            put("coverage_area", "Seluruh Wilayah Surabaya (Barat, Timur, Utara, Selatan, Pusat)")
            put("phone_number", "085712345678")
            put("whatsapp_number", "6285712345678")
            put("description", "General cleaning rumah tinggal, kos mahasiswa, ruko & kantor. Bawa peralatan lengkap dan cairan pembersih ramah lingkungan. Dikerjakan teliti, jujur, dan berintegritas.")
            put("rating_avg", 5.0)
            put("review_count", 42)
            put("verified_badge", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_srv_las_kanopi_wahyu"))
            put("provider_id", uid)
            put("provider_name", "Wahyu Jaya Teknik")
            put("title", "Bengkel Las Listrik, Kanopi SNI & Pagar Minimalis")
            put("category", "PERBAIKAN_RUMAH")
            put("price", 250000L)
            put("price_unit", "meter")
            put("district", "Rungkut")
            put("coverage_area", "Surabaya Timur, Sidoarjo & Surabaya Selatan")
            put("phone_number", "082155556666")
            put("whatsapp_number", "6282155556666")
            put("description", "Fabrikasi kanopi baja ringan, atap spandek/alderon, pagar dorong, railing tangga & teralis jendela. Bahan besi tebal anti karat SNI. Bebas biaya survei lokasi dan konsultasi.")
            put("rating_avg", 4.8)
            put("review_count", 19)
            put("verified_badge", true)
        },
        JSONObject().apply {
            put("id", PocketBaseClient.deterministicPbId("seed_srv_les_privat_its"))
            put("provider_id", uid)
            put("provider_name", "Guru Privat Cerdas Surabaya")
            put("title", "Les Privat Matematika, IPA & Bahasa Inggris SD-SMA")
            put("category", "PENDIDIKAN")
            put("price", 65000L)
            put("price_unit", "jam")
            put("district", "Sukolilo")
            put("coverage_area", "Sukolilo, Mulyorejo, Gubeng & Sekitarnya (Bisa Datang ke Rumah)")
            put("phone_number", "081122334455")
            put("whatsapp_number", "6281122334455")
            put("description", "Bimbingan belajar datang langsung ke rumah siswa. Pengajar lulusan & mahasiswa berprestasi ITS & UNAIR. Pembawaan sabar, metode penalaran mudah dipahami, gratis modul rangkuman soal.")
            put("rating_avg", 4.9)
            put("review_count", 15)
            put("verified_badge", true)
        },
    )

    fun starterPolls(uid: String = "system_seed"): List<CitizenPoll> {
        val now = System.currentTimeMillis()
        val p1VotesByUser = mutableMapOf<String, String>()
        p1VotesByUser["warga_darmo_1"] = "opt_1"
        p1VotesByUser["warga_wonokromo_2"] = "opt_1"
        p1VotesByUser["warga_rungkut_3"] = "opt_2"
        p1VotesByUser["warga_genteng_4"] = "opt_1"
        p1VotesByUser["warga_sukolilo_5"] = "opt_3"
        p1VotesByUser["warga_teb_6"] = "opt_1"

        return listOf(
            CitizenPoll(
                id = "poll_wirawiri_feeder_barat",
                proposerId = uid,
                proposerName = "Cak Wahyu - Forum Transportasi SBY",
                title = "Penambahan Rute Baru Feeder WiraWiri Suroboyo ke Wilayah Barat",
                description = "Apakah rute Feeder WiraWiri perlu diperluas hingga pelosok perumahan Manukan, Babat Jerawat, dan Benowo untuk mempermudah akses ke stasiun dan pusat kota?",
                category = "TRANSPORTASI",
                district = "Benowo",
                status = "APPROVED",
                options = listOf(
                    PollOption(id = "opt_1", text = "Sangat Perlu (Koneksi ke Stasiun Kandangan)", voteCount = 142),
                    PollOption(id = "opt_2", text = "Perlu (Fokus koridor jalan protokol barat)", voteCount = 58),
                    PollOption(id = "opt_3", text = "Cukup Maksimalkan Rute yang Ada Sekarang", voteCount = 19),
                    PollOption(id = "opt_4", text = "Belum Perlu, Prioritaskan Penambahan Suroboyo Bus", voteCount = 11),
                ),
                totalVotes = 230,
                voterUids = p1VotesByUser.keys.toList(),
                votesByUser = p1VotesByUser,
                commentCount = 3,
                createdAt = now - 1000L * 60 * 60 * 36,
                updatedAt = now,
            ),
            CitizenPoll(
                id = "poll_tunjungan_car_free_night",
                proposerId = "warga_kreatif_genteng",
                proposerName = "Ning Sinta - Komunitas Heritage Tunjungan",
                title = "Uji Coba 'Car Free Night' Malam Minggu di Sepanjang Jl. Tunjungan",
                description = "Usulan penutupan kendaraan bermotor di Jl. Tunjungan setiap Sabtu malam pukul 19.00 - 23.00 WIB untuk ruang jalan santai, UMKM kuliner legendaris, dan seni musik akustik jalanan.",
                category = "TATA_KOTA",
                district = "Genteng",
                status = "APPROVED",
                options = listOf(
                    PollOption(id = "cfn_setuju", text = "Setuju Penuh, Kota Jadi Ramah Pejalan Kaki & Wisatawan", voteCount = 312),
                    PollOption(id = "cfn_syarat", text = "Setuju, Asal Disediakan Kantong Parkir Resmi & Aman", voteCount = 184),
                    PollOption(id = "cfn_tolak", text = "Tidak Setuju, Khawatir Bikin Macet Jl. Praban & Sekitarnya", voteCount = 47),
                ),
                totalVotes = 543,
                voterUids = listOf("warga_a", "warga_b"),
                votesByUser = mapOf("warga_a" to "cfn_setuju", "warga_b" to "cfn_syarat"),
                commentCount = 2,
                createdAt = now - 1000L * 60 * 60 * 18,
                updatedAt = now,
            ),
            CitizenPoll(
                id = "poll_pengelolaan_sampah_organik",
                proposerId = "warga_jambangan_asri",
                proposerName = "Pak RT Bambang - RW 04 Jambangan",
                title = "Gerakan Wajib Pilah Sampah Organik dari Dapur Tingkat RT",
                description = "Bagaimana kesiapan warga Surabaya jika Pemkot memberlakukan pemilahan wajib sampah organik (sisa makanan) dan anorganik dari rumah tangga dengan insentif kompos gratis?",
                category = "LINGKUNGAN",
                district = "Jambangan",
                status = "APPROVED",
                options = listOf(
                    PollOption(id = "smph_siap", text = "Siap dan Mendukung Demi Surabaya Bebas Timbunan TPA", voteCount = 98),
                    PollOption(id = "smph_edukasi", text = "Perlu Edukasi & Bantuan Tong Sampah Terpisah Dulu", voteCount = 85),
                    PollOption(id = "smph_sulit", text = "Masih Sulit Diterapkan di Rumah Tangga Padat", voteCount = 14),
                ),
                totalVotes = 197,
                voterUids = emptyList(),
                votesByUser = emptyMap(),
                commentCount = 1,
                createdAt = now - 1000L * 60 * 60 * 8,
                updatedAt = now,
            )
        )
    }

    fun starterPollComments(): Map<String, List<PollComment>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "poll_wirawiri_feeder_barat" to listOf(
                PollComment(
                    id = "pcom_1",
                    pollId = "poll_wirawiri_feeder_barat",
                    authorId = "user_benowo_01",
                    authorName = "Cak Dimas",
                    authorDistrict = "Benowo",
                    text = "Cocok pol iki rek! Khusus e sing arep nyambung numpak KRL Komuter nang Stasiun Kandangan ben gak usah order ojol terus.",
                    createdAt = now - 1000L * 60 * 60 * 20,
                ),
                PollComment(
                    id = "pcom_2",
                    pollId = "poll_wirawiri_feeder_barat",
                    authorId = "user_tandes_02",
                    authorName = "Ning Ririn",
                    authorDistrict = "Tandes",
                    text = "Nek iso head-time kedatangan feeder diatur maksimal 10 menit yho cak, ben gak suwe ngenteni.",
                    createdAt = now - 1000L * 60 * 60 * 14,
                ),
                PollComment(
                    id = "pcom_3",
                    pollId = "poll_wirawiri_feeder_barat",
                    authorId = "user_manukan_03",
                    authorName = "Pak Gunawan",
                    authorDistrict = "Tandes",
                    text = "Dukungan penuh dari warga Manukan Kulon. Jalan Manukan Tama wis padat, transportasi umum feeder cilik pas banget.",
                    createdAt = now - 1000L * 60 * 60 * 5,
                )
            ),
            "poll_tunjungan_car_free_night" to listOf(
                PollComment(
                    id = "pcom_cfn_1",
                    pollId = "poll_tunjungan_car_free_night",
                    authorId = "user_genteng_01",
                    authorName = "Aris Priambodo",
                    authorDistrict = "Genteng",
                    text = "Keren banget konsepnya, vibes heritage Tunjungan malam hari emang tiada tanding kalau bebas polusi knalpot.",
                    createdAt = now - 1000L * 60 * 60 * 10,
                ),
                PollComment(
                    id = "pcom_cfn_2",
                    pollId = "poll_tunjungan_car_free_night",
                    authorId = "user_tegalsari_02",
                    authorName = "Mbak Laras",
                    authorDistrict = "Tegalsari",
                    text = "Yang penting kantong parkir Siola sama Gedung Parkir Genteng Kali dibuka maksimal dan tarif flat resmi tanpa getok.",
                    createdAt = now - 1000L * 60 * 60 * 6,
                )
            ),
            "poll_pengelolaan_sampah_organik" to listOf(
                PollComment(
                    id = "pcom_smph_1",
                    pollId = "poll_pengelolaan_sampah_organik",
                    authorId = "user_jambangan_01",
                    authorName = "Ibu Endang",
                    authorDistrict = "Jambangan",
                    text = "Kampung Jambangan wis suwe praktek biopori & komposter takakura, terbukti asri dan kampung jadi adem.",
                    createdAt = now - 1000L * 60 * 60 * 4,
                )
            )
        )
    }

    fun starterTambalBanSpots(uid: String = "system_seed"): List<TambalBanSpot> {
        val now = System.currentTimeMillis()
        return listOf(
            TambalBanSpot(
                id = "tb_darmo_bungkul_24h",
                authorId = uid,
                authorName = "Cak Slamet - Sahabat Aspal",
                name = "Tambal Ban Tubeless & Motor 24 Jam Cak Slamet",
                description = "Buka nonstop 24 jam di seberang Taman Bungkul. Melayani tambal tubeless tiptop/cacing, ban dalam, isi angin nitrogen, dan ganti ban luar darurat. Salam Satu Aspal dulur!",
                district = "Wonokromo",
                address = "Jl. Raya Darmo No. 88 (Seberang Taman Bungkul), Wonokromo, Surabaya",
                latitude = -7.2915,
                longitude = 112.7388,
                is24Hours = true,
                isTubeless = true,
                forMotor = true,
                forMobil = true,
                canCallOut = true,
                mapsUrl = "https://maps.app.goo.gl/surabayadarmo24h",
                phoneNumber = "081234567891",
                whatsappNumber = "081234567891",
                verifiedCount = 28,
                verifiedUserIds = listOf("warga_a", "warga_b", "warga_c"),
                commentCount = 3,
                createdAt = now - 1000L * 60 * 60 * 48,
                updatedAt = now,
            ),
            TambalBanSpot(
                id = "tb_ayani_frontage_press",
                authorId = uid,
                authorName = "Pakde Harto",
                name = "Tambal Ban & Press Velg Sahabat Ahmad Yani",
                description = "Spesialis tambal ban motor dan mobil di lajur Frontage Road barat A. Yani. Siap panggilan darurat untuk pemotor & driver mogok di area Wonokromo sampai Siwalankerto.",
                district = "Gayungan",
                address = "Jl. Frontage Road Ahmad Yani No. 120, Gayungan, Surabaya",
                latitude = -7.3195,
                longitude = 112.7324,
                is24Hours = true,
                isTubeless = true,
                forMotor = true,
                forMobil = true,
                canCallOut = true,
                mapsUrl = "https://maps.app.goo.gl/surabayaayanipress",
                phoneNumber = "081398765432",
                whatsappNumber = "081398765432",
                verifiedCount = 19,
                verifiedUserIds = listOf("warga_x", "warga_y"),
                commentCount = 2,
                createdAt = now - 1000L * 60 * 60 * 36,
                updatedAt = now,
            ),
            TambalBanSpot(
                id = "tb_rungkut_madya_upn",
                authorId = uid,
                authorName = "Mas Wahyu",
                name = "Tambal Ban Cepat Rungkut Madya UPN",
                description = "Lokasi persis di barat gerbang kampus UPN Veteran Jatim. Buka pagi jam 06.00 sampai 23.00 WIB. Pelayanan ramah, sediakan ban dalam baru cadangan semua ukuran matic & bebek.",
                district = "Rungkut",
                address = "Jl. Raya Rungkut Madya No. 45, Rungkut, Surabaya",
                latitude = -7.3328,
                longitude = 112.7845,
                is24Hours = false,
                isTubeless = true,
                forMotor = true,
                forMobil = false,
                canCallOut = false,
                mapsUrl = "https://maps.app.goo.gl/surabayarungkutmadya",
                phoneNumber = "085711223344",
                whatsappNumber = "085711223344",
                verifiedCount = 14,
                verifiedUserIds = listOf("warga_r1"),
                commentCount = 2,
                createdAt = now - 1000L * 60 * 60 * 24,
                updatedAt = now,
            ),
            TambalBanSpot(
                id = "tb_stasiun_kota_waspada",
                authorId = uid,
                authorName = "Cak Man Pabean",
                name = "Tambal Ban & Isi Angin Stasiun Semut Kota",
                description = "Penyelamat ban bocor area Surabaya Utara dekat Pasar Atom dan Stasiun Kota. Buka 24 jam dengan kompresor bertenaga, bisa layani ban mobil van/pickup dan motor roda tiga.",
                district = "Pabean Cantian",
                address = "Jl. Waspada No. 18, Pabean Cantian, Surabaya",
                latitude = -7.2384,
                longitude = 112.7421,
                is24Hours = true,
                isTubeless = true,
                forMotor = true,
                forMobil = true,
                canCallOut = false,
                mapsUrl = "https://maps.app.goo.gl/surabayapabeanwaspada",
                phoneNumber = "082155667788",
                whatsappNumber = "082155667788",
                verifiedCount = 22,
                verifiedUserIds = emptyList(),
                commentCount = 1,
                createdAt = now - 1000L * 60 * 60 * 12,
                updatedAt = now,
            ),
            TambalBanSpot(
                id = "tb_kenjeran_suramadu",
                authorId = uid,
                authorName = "Bang Dul Kenjeran",
                name = "Tambal Ban Pesisir Suramadu Kenjeran",
                description = "Sangat strategis bagi pengendara sebelum naik jembatan Suramadu atau setelah melintasi Kenjeran. Buka 24 jam, sediakan cairan tubeless & pentil tubeless.",
                district = "Kenjeran",
                address = "Jl. Kedung Cowek No. 204 (Akses Jembatan Suramadu), Kenjeran, Surabaya",
                latitude = -7.2285,
                longitude = 112.7842,
                is24Hours = true,
                isTubeless = true,
                forMotor = true,
                forMobil = true,
                canCallOut = true,
                mapsUrl = "https://maps.app.goo.gl/surabayakedungcowek",
                phoneNumber = "087812349988",
                whatsappNumber = "087812349988",
                verifiedCount = 16,
                verifiedUserIds = emptyList(),
                commentCount = 1,
                createdAt = now - 1000L * 60 * 60 * 6,
                updatedAt = now,
            )
        )
    }

    fun starterTambalBanComments(): Map<String, List<TambalBanComment>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "tb_darmo_bungkul_24h" to listOf(
                TambalBanComment(
                    id = "tb_c_1",
                    spotId = "tb_darmo_bungkul_24h",
                    authorId = "user_ojol_01",
                    authorName = "Cak Anto Ojol",
                    authorDistrict = "Tegalsari",
                    text = "Bocel ban belakang pas jam 2 pagi waktu narik orderan, untung Cak Slamet buka standby. Pengerjaan rapi dan cepat!",
                    createdAt = now - 1000L * 60 * 60 * 24,
                ),
                TambalBanComment(
                    id = "tb_c_2",
                    spotId = "tb_darmo_bungkul_24h",
                    authorId = "user_rider_02",
                    authorName = "Bayu Pratama",
                    authorDistrict = "Wonokromo",
                    text = "Tarif wajar standar Surabaya, tambal tubeless 15rb langsung gas lagi. Kompresor jos.",
                    createdAt = now - 1000L * 60 * 60 * 12,
                ),
            ),
            "tb_ayani_frontage_press" to listOf(
                TambalBanComment(
                    id = "tb_c_3",
                    spotId = "tb_ayani_frontage_press",
                    authorId = "user_driver_03",
                    authorName = "Pak Handoko",
                    authorDistrict = "Gayungan",
                    text = "Mobil saya kena paku di dekat flyover, ditelepon Pakde langsung datang bawa dongkrak dan kompresor mini. Salam satu aspal pakde!",
                    createdAt = now - 1000L * 60 * 60 * 8,
                )
            ),
            "tb_rungkut_madya_upn" to listOf(
                TambalBanComment(
                    id = "tb_c_4",
                    spotId = "tb_rungkut_madya_upn",
                    authorId = "user_mhs_04",
                    authorName = "Dina Mahasiswi",
                    authorDistrict = "Rungkut",
                    text = "Sangat membantu mahasiswa UPN pas buru-buru ujian ban kempis. Masnya ramah banget.",
                    createdAt = now - 1000L * 60 * 60 * 16,
                )
            )
        )
    }
}

