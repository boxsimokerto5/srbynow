# Skema 25 Tabel PocketBase Arek Surabaya

File skema lengkap berekstensi JSON sudah tersimpan di project:
- **`pocketbase_collections.json`** (di root project dan di folder artifacts)
- Admin resmi: **`eccko.w4@gmail.com`**

---

## 1. Tabel Utama Masalah Banner: `app_settings`
Banner di halaman beranda hilang setelah disimpan karena PocketBase belum memiliki tabel `app_settings`.
Berikut adalah skema tabel `app_settings`:

```json
{
  "id": "sbycolappset001",
  "name": "app_settings",
  "type": "base",
  "system": false,
  "schema": [
    {
      "system": false,
      "id": "sbyas001",
      "name": "config_key",
      "type": "text",
      "required": true,
      "presentable": true,
      "unique": true,
      "options": {
        "min": 1,
        "max": 80,
        "pattern": ""
      }
    },
    {
      "system": false,
      "id": "sbyas002",
      "name": "config_value",
      "type": "text",
      "required": true,
      "presentable": false,
      "unique": false,
      "options": {
        "min": 1,
        "max": 4000,
        "pattern": ""
      }
    },
    {
      "system": false,
      "id": "sbyas003",
      "name": "updated_by",
      "type": "text",
      "required": false,
      "presentable": false,
      "unique": false,
      "options": {
        "min": null,
        "max": 64,
        "pattern": ""
      }
    }
  ],
  "indexes": [],
  "listRule": "@request.auth.id != ''",
  "viewRule": "@request.auth.id != ''",
  "createRule": "@request.auth.id != '' && (@request.auth.is_admin = true || @request.auth.email = 'eccko.w4@gmail.com' || @request.auth.email = 'nabilalailatus12@gmail.com')",
  "updateRule": "@request.auth.id != '' && (@request.auth.is_admin = true || @request.auth.email = 'eccko.w4@gmail.com' || @request.auth.email = 'nabilalailatus12@gmail.com')",
  "deleteRule": "@request.auth.id != '' && (@request.auth.is_admin = true || @request.auth.email = 'eccko.w4@gmail.com' || @request.auth.email = 'nabilalailatus12@gmail.com')",
  "options": {}
}
```

---

## 2. Daftar 25 Tabel Lengkap

1. **`users`** - Akun pengguna, avatar, peran, verified citizen, admin status (`eccko.w4@gmail.com`).
2. **`bookmarks`** - Simpanan favorit pengguna.
3. **`nearby_profiles`** - Profil radar & pencarian teman sekitar (Menu Sekitar).
4. **`incidents`** - Laporan darurat 112 & kendala kota.
5. **`incident_comments`** - Komentar pada laporan darurat.
6. **`events`** - Agenda & event kota Surabaya.
7. **`culinary`** - Wisata kuliner khas Surabaya.
8. **`culinary_reviews`** - Review kuliner.
9. **`chat_rooms`** - Kamar chat warga & komunitas.
10. **`chat_messages`** - Pesan chat realtime.
11. **`destinations`** - Destinasi wisata & tempat ikonik.
12. **`destination_reviews`** - Ulasan tempat wisata.
13. **`moments`** - Momen warga (timeline sosial).
14. **`moment_comments`** - Komentar momen.
15. **`services`** - Layanan jasa warga / UMKM.
16. **`tambal_ban`** - Lokasi tambal ban 24 jam & darurat.
17. **`tambal_ban_comments`** - Review & update tambal ban.
18. **`polls`** - Polling warga & suara kota.
19. **`poll_comments`** - Komentar & diskusi polling.
20. **`notifications`** - Notifikasi aktivitas aplikasi.
21. **`barkas_items`** - Jual beli barang bekas (Barkas SBY).
22. **`barkas_comments`** - Pertanyaan / diskusi barkas.
23. **`kos_listings`** - Info kos & sewa kamar Surabaya.
24. **`kos_comments`** - Diskusi sewa kos.
25. **`app_settings`** - Pengaturan sistem banner beranda & konfigurasi global.

---

## 3. Cara Mengambil & Mengimport File JSON Lengkap ke PocketBase

File lengkap 25 tabel (`pocketbase_collections.json`) sudah tersedia di:
1. Tab **Artifacts** di layar ini: buka file `pocketbase_collections.json`, klik Copy/Salin semua isinya.
2. Menu file explorer project (sebelah kiri kode): buka file `pocketbase_collections.json`.
3. Buka PocketBase Admin Dashboard -> Klik ikon **Settings** (Gear) di kiri bawah -> Klik **Sync collections** (atau **Import collections**).
4. Paste seluruh isi JSON -> Klik **Review** -> Klik **Confirm and sync**.
