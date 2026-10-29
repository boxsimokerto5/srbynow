# Surabaya Hari Ini — Aplikasi Media Sosial & Informasi Warga Surabaya

**Surabaya Hari Ini** adalah aplikasi media sosial lokal terpadu untuk warga dan pengunjung Kota Surabaya yang menggabungkan pembaruan kejadian kota secara *real-time*, agenda event dengan kurasi admin, direktori kuliner lengkap dengan ulasan warga, pencarian teman sekitar berbasis GPS dengan kontrol privasi jarak, serta obrolan (*chat*) langsung dalam satu antarmuka yang modern, rapat, proporsional, dan serba membulat (*rounded*).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berikut adalah rangkuman keputusan produk yang telah dikonfirmasi beserta rancangan teknis yang akan diterapkan saat Anda menekan tombol **Proceed**:

- **Sistem Moderasi Event Surabaya (Dikonfirmasi)**: Pengguna dapat mengajukan usulan event baru (status awal *Menunggu Persetujuan*). Akun utama (`nabilalailatus12@gmail.com`) otomatis memiliki akses Admin, ditambah fitur **Toggle Mode Admin** pada sheet Profil sehingga Anda dapat menguji alur persetujuan/penolakan event secara langsung kapan saja.
- **Deteksi Teman Sekitar & Privasi (Dikonfirmasi)**: Menggunakan sensor lokasi GPS perangkat (`FusedLocationProviderClient`) untuk menghitung jarak akurat antarwarga (Haversine), dilengkapi pengaturan **Privasi Radius & Visibilitas** (misal: sembunyikan jarak eksak atau batasi jangkauan radius pencarian 1 km – 25 km).
- **Kelengkapan Fitur Kuliner & Kejadian (Dikonfirmasi)**:
  - **Kuliner Surabaya**: Dilengkapi foto visual, tag lokasi & pembuka peta navigasi, indikator jam operasional (*Buka / Tutup*), filter kategori kuliner khas Surabaya, rating bintang & ulasan warga, serta fitur **Bookmark / Simpan**.
  - **Kejadian Surabaya**: Dilengkapi kategori insiden (Lalu Lintas, Cuaca/Banjir, Fasilitas Umum, Info Warga), lencana & tombol **Verifikasi Warga** ("Saya melihat ini"), tombol **Kontak Darurat Cepat (Command Center 112 Surabaya)**, dan fitur **Bookmark Info Penting**.
- **Identitas Visual & Interaksi Pop-up (Dikonfirmasi)**: Palet **Hijau Toska Khas Surabaya** (*Surabaya Emerald-Teal*) dengan tipografi geometris modern, tata letak kartu yang rapat namun proporsional (*compact & balanced*), sudut membulat penuh (`16dp`–`28dp`), serta **seluruh pop-up detail, formulir tambah data, filter, dan profil muncul meluncur dari bawah (*Slide-up Bottom Sheet* ala Google Maps)**.
- **Autentikasi & Basis Data Cloud Real-Time**: Menggunakan **Google Sign-In** resmi dan **Firebase Cloud Firestore** agar pesan chat, status kejadian, ulasan kuliner, pengajuan event, serta daftar teman tersinkronisasi secara *real-time* antarperangkat.

---

## 1. Overview & Core Concept

- **Apa yang Dilakukan Aplikasi**: Menjadi pusat aktivitas digital warga Surabaya ("*Super-App Komunitas Kota Pahlawan*"). Warga bisa memantau kondisi jalan/kota terkini, mencari rekomendasi kuliner malam atau legendaris, mengikuti maupun mengusulkan acara komunitas, berkenalan dengan teman di sekitar lokasi mereka, dan langsung mengobrol di ruang pesan pribadi maupun ruang publik Surabaya.
- **Target Pengguna**: Warga lokal Surabaya, mahasiswa perantauan, pelaku UMKM kuliner/event kreatif, dan wisatawan yang membutuhkan informasi hiper-lokal yang cepat dan terpercaya.
- **Nilai Utama (*Key Value*)**: Menggantikan informasi yang terpencar di berbagai grup menjadi satu aplikasi yang rapi, terstruktur, terverifikasi warga/admin, dan nyaman dioperasikan dengan satu tangan berkat interaksi *bottom sheet* bergaya Google Maps.

---

## 2. User Experience & Visual Design

### Alur Pengguna Utama (*Key User Flows*)
1. **Masuk Cepat & Aman**: Halaman sambutan sinematik bertema Surabaya dengan tombol **Masuk dengan Google**. Setelah masuk, pengguna langsung diarahkan ke tampilan utama dengan bilah atas (*Compact Header*) berisi sapaan kota, indikator kecamatan/lokasi aktif, tombol Bookmark Tersimpan, dan avatar Profil/Admin.
2. **Kejadian Surabaya (Beranda Lini Masa Kota)**:
   - Menampilkan bar cepat **Darurat 112 Surabaya** yang ringkas di bagian atas dan filter chip kategori (*Semua, Lalu Lintas, Banjir/Cuaca, Kehilangan, Info Kota*).
   - Kartu laporan rapat & informatif berisi foto/visual lokasi, tingkat urgensi, waktu kejadian, lokasi jalan/kecamatan, jumlah verifikasi warga, dan tombol simpan.
   - Ketuk kartu mana pun → **Bottom Sheet ala Google Maps** meluncur dari bawah menampilkan kronologi lengkap, titik koordinat/alamat dengan tombol "Buka di Google Maps", komentar warga terkini, dan tombol **"Bantu Verifikasi Laporan"**.
3. **Event Surabaya & Panel Persetujuan Admin**:
   - Menampilkan kalender/daftar event yang sudah **Disetujui Admin** (Konser, Pameran UMKM, Car Free Day, Kajian/Komunitas).
   - Tombol **"Usulkan Event"** membuka *Slide-up Sheet* formulir pengajuan event (judul, kategori, tanggal & jam, lokasi venue, harga tiket/gratis, deskripsi). Pengusul dapat melihat status usulan miliknya (*Menunggu / Disetujui / Ditolak*).
   - Jika **Mode Admin** aktif, muncul tab/banner khusus **"Antrean Moderasi Admin"** di mana admin dapat meninjau detail usulan dalam *bottom sheet* dan menekan **Setujui Tayang** atau **Tolak** dengan catatan.
4. **Kuliner Surabaya**:
   - Dilengkapi pencarian instan, chip kategori (*Rawon & Soto, Penyetan & Sambal, Kopi & Nongkrong, Jajanan Malam, Legendaris*), filter **Sedang Buka**, dan daftar favorit tersimpan.
   - Kartu kuliner menampilkan foto menggugah selera, rating bintang rata-rata, kisaran harga, lencana *Buka/Tutup*, dan kecamatan.
   - Ketuk item → **Bottom Sheet Detail Kuliner ala Google Maps** dengan *handle bar*, galeri header, jam buka, tombol arah peta, tombol Bookmark, serta daftar **Ulasan Warga** di mana pengguna bisa langsung memberi bintang (1–5) dan menulis review baru.
5. **Teman Sekitar (Berbasis GPS)**:
   - Meminta izin lokasi GPS secara ramah; menampilkan radar/status lokasi aktif beserta tombol *Slide-up Pengaturan Privasi* (atur radius pencarian `1 km`, `5 km`, `10 km`, `25 km`, serta opsi sembunyikan posisi aktif).
   - Menampilkan daftar warga di sekitar lengkap dengan jarak terhitung (misal `0.8 km dari Anda`), minat/hobi, bio singkat, dan tombol cepat **"Sapa / Chat"** yang langsung membuka ruang obrolan.
6. **Chat Warga (Pesan Pribadi & Ruang Warga Surabaya)**:
   - Terdiri dari tab **Pesan Pribadi** (hasil sapaan dari Teman Sekitar) dan **Ruang Publik Warga** (diskusi cepat seputar Surabaya).
   - Ruang percakapan modern dengan gelembung chat membulat (*rounded bubbles*), penanda waktu presisi, status pengirim, dan pengiriman pesan *real-time*.

### Identitas Visual & Tema (*Visual Identity & Theme*)
- **Arah Estetika**: *Modern Hyper-Local Compact* — memadukan ketegasan editorial aplikasi berita kota dengan kehangatan sosial media modern. Rapat (*high information density* tanpa ruang kosong berlebihan), proporsional dalam skala elemen, dan lembut berkat sudut membulat konsisten.
- **Palet Warna (Hijau Toska Khas Surabaya)**:
  - **Primary (`#006B5E` / Dark Mode `#54DBC8`)**: Hijau Toska Suroboyo yang segar, melambangkan identitas kota pesisir & taman kota Surabaya yang asri.
  - **Secondary (`#00838F` / Soft Cyan-Teal)**: Aksen pendukung untuk lencana lokasi, jarak GPS, dan filter aktif.
  - **Tertiary / Warm Accent (`#D97724` / Amber Terakota)**: Aksen hangat untuk bintang rating kuliner, lencana *Populer*, dan sorotan event.
  - **Alert / Incident (`#C62828` / Soft Coral Surface)**: Khusus penanda kejadian darurat/macet parah dan tombol cepat 112.
  - **Background & Surface (`#F4F8F7` Light / `#0E1514` Dark)**: Permukaan putih-toska sangat lembut sehingga kartu putih bersih (`#FFFFFF`) tampak menonjol secara alami tanpa garis batas kasar.
- **Tipografi & Hierarki**:
  - **Display & Judul (`Plus Jakarta Sans`)**: Font geometris modern karya desainer Indonesia yang sangat serasi untuk aplikasi urban kekinian, tegas, dan hemat ruang.
  - **Teks Isi & Label (`Inter` / `DM Sans`)**: Sangat jernih pada ukuran kompak (`12sp`–`14sp`) untuk menjaga kerapian metadata (jarak, jam buka, jumlah verifikasi).
- **Gaya Komponen & Bottom Sheet ala Google Maps**:
  - **Sudut Membulat (*Rounded*)**: Kartu konten `20dp`, chip & tombol aksi `999dp` (*full pill*), input teks `16dp`, dan Bottom Sheet bagian atas `28dp`.
  - **Google Maps-Style Slide-Up Sheets**: Menggunakan `ModalBottomSheet` dengan *drag handle* kapsul halus di puncak, header ringkas yang tetap terlihat, transisi paman/pegas (*spring physics*) mulus dari bawah layar, dan dukungan gulir (*nested scroll*) untuk membaca ulasan panjang atau mengisi formulir tanpa pindah halaman kaku.

---

## 3. Key Product Decisions & Trade-Offs

- **Keputusan 1: Pola *Slide-Up Bottom Sheet* untuk Seluruh Detail & Aksi Kontekstual**
  - *Pendekatan*: Alih-alih memindahkan pengguna ke layar penuh baru setiap kali melihat detail kuliner, membaca kejadian, meninjau event, atau mengatur filter GPS, informasi dibuka menggunakan panel geser dari bawah (*Bottom Sheet* ala Google Maps).
  - *Mengapa*: Menjaga konteks halaman utama tetap terlihat di latar belakang, mempercepat eksplorasi satu tangan, dan memenuhi permintaan tampilan modern dengan pop-up meluncur dari bawah.
- **Keputusan 2: Cloud Firestore Real-Time + Data Awal Kurasi Surabaya (*Seeded Live Community Data*)**
  - *Pendekatan*: Menyimpan seluruh entitas di Firebase Firestore dengan aturan keamanan ketat (*Zero-Trust Rules*), sekaligus menyediakan mekanisme inisialisasi konten awal bertema autentik Surabaya (misal kuliner Rawon/Tahu Tek legendaris, agenda Taman Bungkul/Tunjungan, info lalu lintas Ahmad Yani) apabila koleksi masih kosong saat pertama kali dijalankan.
  - *Mengapa*: Pengguna langsung merasakan aplikasi yang hidup, indah, dan proporsional sejak detik pertama masuk, sembari tetap bisa menambah data nyata yang tersimpan permanen di cloud.
- **Keputusan 3: Perhitungan Jarak GPS Hibrida dengan Kontrol Privasi**
  - *Pendekatan*: Mengambil titik koordinat nyata dari GPS perangkat pengguna (atau titik default pusat Surabaya Monkasel/Tunjungan bila izin GPS ditolak oleh pengguna), lalu membulatkan tampilan jarak ke kelipatan `0.1 km` dan mengizinkan pengguna mengatur radius serta visibilitas.
  - *Mengapa*: Akurat untuk mencari teman sekitar sekaligus menjaga keamanan privasi lokasi rumah/presisi pengguna.

---

## 4. Technical Architecture & Data Strategy

### Diagram Arsitektur & Hirarki Komponen

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        SURABAYA HARI INI (Jetpack Compose)                   │
├──────────────────────────────────────────────────────────────────────────────┤
│  [Auth Gate: Google Sign-In via CredentialManager]                           │
│         │                                                                    │
│         ▼                                                                    │
│  ┌────────────────────────────────────────────────────────────────────────┐  │
│  │ Compact Top Bar (Lokasi Aktif • Bookmark Tersimpan • Profil/Mode Admin)│  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  Navigasi Bawah 5 Menu Proporsional (M3 Pill NavigationBar)            │  │
│  │   ├─ 1. Kejadian   (Feed Laporan Warga, Bar 112, Verifikasi, Kategori) │  │
│  │   ├─ 2. Event      (Agenda Kota, Usul Event, Antrean Persetujuan Admin)│  │
│  │   ├─ 3. Kuliner    (Direktori, Status Buka/Tutup, Rating & Ulasan)     │  │
│  │   ├─ 4. Sekitar    (Radar/List GPS Warga, Filter Radius, Sapa Cepat)   │  │
│  │   └─ 5. Chat       (Pesan Pribadi 1-on-1 & Ruang Obrolan Publik Kota)  │  │
│  ├────────────────────────────────────────────────────────────────────────┤  │
│  │  Lapisan Slide-Up Bottom Sheets (Gaya Google Maps)                     │  │
│  │   • Sheet Detail Kejadian & Komentar  • Sheet Form Lapor / Usul Event  │  │
│  │   • Sheet Detail Kuliner & Ulasan     • Sheet Moderasi Admin Event     │  │
│  │   • Sheet Pengaturan Radius GPS       • Sheet Profil & Bookmark Warga  │  │
│  └──────────────────────────────────┬─────────────────────────────────────┘  │
└─────────────────────────────────────┼────────────────────────────────────────┘
                                      │ StateFlow + Coroutines
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                 MVVM ViewModels & Repository Layer                           │
│  • Auth & Profile Repo      • Incident (Kejadian) Repo   • Event & Admin Repo│
│  • Culinary & Review Repo   • Nearby Location (GPS) Repo • Realtime Chat Repo│
└─────────────────────────────────────┬────────────────────────────────────────┘
                                      │
          ┌───────────────────────────┴───────────────────────────┐
          ▼                                                       ▼
┌───────────────────────────────────┐   ┌──────────────────────────────────────┐
│ Firebase Cloud Firestore & Auth   │   │ Android Platform Services            │
│ • Named DB Instance               │   │ • FusedLocationProviderClient (GPS)  │
│ • Hardened Security Rules         │   │ • Photo Picker (Unggah Visual/Sampul)│
│ • Real-time Snapshot Listeners    │   │ • Google Maps / Dialer 112 Intents   │
└───────────────────────────────────┘   └──────────────────────────────────────┘
```

### Model Data & Koleksi Cloud Firestore
1. **`users/{userId}`**: Profil pengguna (`displayName`, `photoUrl`, `bio`, `district` kecamatan di Surabaya, `interests`, `lat`, `lng`, `isDiscoverable`, `searchRadiusKm`, `isAdmin`, `updatedAt`).
2. **`users/{userId}/bookmarks/{bookmarkId}`**: Item kuliner, kejadian, atau event yang disimpan pribadi oleh pengguna (`targetId`, `targetType`, `title`, `subtitle`, `createdAt`).
3. **`incidents/{incidentId}`** (*Kejadian Surabaya*): Laporan kejadian (`authorId`, `authorName`, `title`, `description`, `category`, `severity`, `address`, `district`, `lat`, `lng`, `imageKey`, `verifiedCount`, `verifiedByUids`, `createdAt`).
4. **`events/{eventId}`** (*Event Surabaya*): Agenda acara (`proposerId`, `proposerName`, `title`, `category`, `eventDateText`, `timeText`, `venueName`, `address`, `priceLabel`, `description`, `status` [`PENDING` | `APPROVED` | `REJECTED`], `adminNote`, `interestedCount`, `createdAt`).
5. **`culinary/{culinaryId}` & `culinary/{culinaryId}/reviews/{reviewId}`** (*Kuliner Surabaya*): Tempat makan (`name`, `category`, `address`, `district`, `priceRange`, `openHour`, `closeHour`, `ratingAvg`, `reviewCount`, `lat`, `lng`, `description`, `createdBy`) beserta sub-koleksi ulasan bintang dari warga.
6. **`chatRooms/{roomId}` & `chatRooms/{roomId}/messages/{messageId}`** (*Chat Warga*): Ruang pesan publik maupun *direct message* antar 2 pengguna (`participantUids`, `isPublicRoom`, `roomTitle`, `lastMessage`, `updatedAt`) dan aliran pesan *real-time*.

### Pemetaan Interaksi & State (*Interactive Component Mapping*)
- **Pengajuan & Moderasi Event**: Saat warga mengirim form di *Bottom Sheet Usul Event*, dokumen disimpan dengan `status = "PENDING"` dan langsung muncul di bagian "Usulan Saya". Ketika Admin mengaktifkan *Mode Admin* (atau login dengan email admin) dan menekan **Setujui**, status diperbarui menjadi `"APPROVED"` dan seketika tampil di lini masa publik semua warga.
- **Filter Buka/Tutup & Ulasan Kuliner**: Status *Buka / Tutup* dihitung secara dinamis dari jam lokal perangkat terhadap `openHour`–`closeHour` masing-masing tempat kuliner. Saat warga mengirim ulasan baru dari *Bottom Sheet*, rata-rata rating dan jumlah ulasan diperbarui secara transaksional.
- **GPS Teman Sekitar -> Sapa di Chat**: Menekan tombol *Perbarui Lokasi GPS* meminta izin `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`, menyimpan koordinat pengguna, dan mengurutkan daftar warga aktif dalam radius pilihan. Menekan tombol **Sapa** otomatis membuat/membuka `chatRoom` 1-lawan-1 dengan pengguna tersebut dan mengarahkan ke ruang obrolan.
- **Tindakan Nyata ke Sistem Android**: Tombol **Petunjuk Arah** pada sheet Kuliner/Event/Kejadian membuka aplikasi peta navigasi (`geo:` URI ke Google Maps), dan tombol **Darurat 112** membuka dialer telepon dengan nomor resmi `112` tanpa melakukan panggilan paksa tanpa konfirmasi pengguna.
