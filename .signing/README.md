# Android Release Signing (Otomatis & Permanen)

Folder `.signing/` ini digunakan oleh GitHub Actions (`.github/workflows/build-android.yml`) untuk menyimpan **Release Keystore terenkripsi AES-256 (`release-key.jks.gpg`)** agar *signature key* aplikasi **Arek Surabaya** tetap sama di setiap *build*.

## Cara Kerja
1. **Persiapan Sekali Saja di GitHub:**
   - Buka repositori GitHub Anda -> **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.
   - Buat *secret* bernama **`KEYSTORE_PASSPHRASE`** dan isi dengan password rahasia bebas pilihan Anda (simpan juga salinan password ini di catatan pribadi Anda).
2. **Saat Build Pertama:**
   - Karena `release-key.jks.gpg` belum ada, GitHub Actions otomatis membuat keystore baru (`alias: upload`), mengenkripsinya menggunakan `KEYSTORE_PASSPHRASE`, lalu meng-*commit* `release-key.jks.gpg` dan `SHA-FINGERPRINTS.txt` ke folder ini.
3. **Saat Build Kedua dan Seterusnya:**
   - GitHub Actions mendeteksi `release-key.jks.gpg` sudah ada, mendekripsinya sementara selama proses *build*, dan menandatangani **APK** serta **AAB** dengan *signature key* yang **100% sama**.

## Penting
- **Jangan hapus** file `.signing/release-key.jks.gpg` setelah terbentuk agar *update* aplikasi di HP pengguna maupun Google Play Store tidak pernah ditolak akibat perbedaan *signature*.
- **Jangan ubah** nilai secret `KEYSTORE_PASSPHRASE` di GitHub setelah build pertama berhasil.
