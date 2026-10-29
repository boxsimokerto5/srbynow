# Android Release Signing (100% Otomatis & Permanen)

Folder `.signing/` ini digunakan oleh GitHub Actions (`.github/workflows/build-android.yml`) untuk menyimpan **Release Keystore terenkripsi AES-256 (`release-key.jks.gpg`)** agar *signature key* aplikasi **Arek Surabaya** tetap sama di setiap *build*.

## Cara Kerja (Tanpa Perlu Setup Manual)
1. **Langsung Jalan Saat Push Pertama (Zero-Config):**
   - Anda **tidak wajib** membuat *secret* apa pun terlebih dahulu. Saat pertama kali *push* ke GitHub, GitHub Actions otomatis membuat keystore baru (`alias: upload`), mengenkripsinya dengan AES-256, lalu menyimpannya secara permanen ke `.signing/release-key.jks.gpg` beserta cadangannya di branch `signing-vault`.
2. **Saat Build Kedua dan Seterusnya:**
   - GitHub Actions otomatis memulihkan `release-key.jks.gpg` yang sudah ada dan menandatangani **APK** serta **AAB** menggunakan *signature key* yang **100% sama** selamanya (bahkan jika Anda melakukan *push* ulang dari AI Studio).
3. **Opsional — Menambahkan Password Rahasia Sendiri (`KEYSTORE_PASSPHRASE`):**
   - Jika suatu saat Anda ingin mengunci enkripsi menggunakan password pilihan Anda sendiri, Anda cukup menambahkan *Repository Secret* bernama **`KEYSTORE_PASSPHRASE`** di **Settings -> Secrets and variables -> Actions**.
   - Pada *build* berikutnya, GitHub Actions akan otomatis mengenkripsi ulang keystore yang **sama** menggunakan password baru tersebut tanpa mengubah *signature key* aplikasi.

## Penting
- **Jangan hapus** file `.signing/release-key.jks.gpg` dan branch `signing-vault` setelah terbentuk agar *update* aplikasi di HP pengguna maupun Google Play Store selalu menggunakan *signature* yang sama.
