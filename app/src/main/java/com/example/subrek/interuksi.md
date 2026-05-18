# PANDUAN EKSEKUSI BERTAHAP (VIBECODING ROADMAP)

Instruksi untuk AI Agent: Kerjakan tugas berdasarkan urutan Fase dan Step di bawah ini. Jangan melompat ke langkah berikutnya sebelum langkah saat ini selesai diuji, dikompilasi tanpa error, dan disetujui oleh pengguna.

---

## FASE 1: Inisialisasi Proyek & Dasar Arsitektur Core (Fondasi)

### Step 1.1: Konfigurasi Gradle Dependencies & AndroidManifest
* [cite_start]**Tugas AI:** Tambahkan semua dependensi yang diperlukan di `build.gradle.kts` (Project & App level): Jetpack Compose, Hilt DI, Room DB, Supabase Client, dan WorkManager[cite: 2, 25, 41, 43].
* [cite_start]**Setup Manifest:** Daftarkan izin `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, dan `INTERNET` di `AndroidManifest.xml`[cite: 25, 34, 43, 45].
* **Output:** Proyek dapat di-*build* ulang (Gradle Sync) dengan sukses tanpa konflik versi.

### Step 1.2: Pembuatan Paket Struktur & Tema Global (Core UI Theme)
* [cite_start]**Tugas AI:** Buat seluruh struktur folder di bawah paket utama `com.subrek.app/` sesuai dengan skema Directory Structure[cite: 40, 41].
* [cite_start]**Implementasi Desain:** Konfigurasikan `Theme.kt`, `Color.kt`, dan `Type.kt` menggunakan token utilitas yang mengadaptasi prinsip Tailwind CSS v4.1 (spacing kelipatan 4dp, palette warna fungsional)[cite: 41, 43, 45].
* **Output:** Kode dasar tema Jetpack Compose siap digunakan di semua fitur.

### Step 1.3: Inisialisasi Dependency Injection & Kelas Aplikasi Utama
* **Tugas AI:** Buat kelas `SubrekApplication` yang dianotasi dengan `@HiltAndroidApp`. Buat modul Hilt global dasar di dalam paket `core/di/`.
* **Output:** Aplikasi dapat dijalankan di emulator/perangkat dengan layar kosong (blank screen) tanpa *crash*.

---

## FASE 2: Membangun Data Layer & Kontrak Domain (Single Source of Truth)

### Step 2.1: Implementasi Domain Entities & Kontrak Repositori
* [cite_start]**Tugas AI:** Buat model data murni Kotlin murni (Pure Kotlin Entities) di `core/` atau fitur terkait (misalnya `Subscription.kt` di `features/subscription/domain/model/`)[cite: 41]. [cite_start]Jangan masukkan anotasi library pihak ketiga di sini[cite: 40, 41]. [cite_start]Buat interface kontrak `SubscriptionRepository`[cite: 41].
* [cite_start]**Output:** Struktur data bisnis inti terbentuk tanpa interferensi database[cite: 40, 41].

### Step 2.2: Implementasi Room Local Database (Offline-First Basis)
* [cite_start]**Tugas AI:** Tuliskan Room Entity menggunakan anotasi @Entity (ubah tipe data file/image menjadi String/VARCHAR), Data Access Object (DAO) dengan kueri CRUD dasar menggunakan SQL query eksplisit, serta kelas database utamanya (RoomDatabase). Pastikan semua SQL query dan konfigurasi keamanan lokal (RLS/constraint) sudah tertulis lengkap agar saya bisa langsung pakai tanpa setup tambahan.
* [cite_start]**Output:** ersedianya arsitektur local data source yang siap pakai berupa Room Entity dengan konversi tipe data file ke String/VARCHAR, DAO dengan kueri CRUD SQL eksplisit, serta kelas utama RoomDatabase yang dikonfigurasi secara aman dengan constraint data lokal untuk memastikan fungsionalitas offline-first berjalan langsung tanpa setup tambahan.

### Step 2.3: Integrasi Awal Remote Data Source (Supabase Client)
* [cite_start]**Tugas AI:** Setup konfigurasi Supabase Client SDK di level data source remote[cite: 41]. Buat fungsi *mapping* data dari Room Entity ke Supabase DTO dan sebaliknya.
* **Output:** Kerangka sinkronisasi data lokal-cloud terbentuk.

---

## FASE 3: Eksekusi Fitur Secara Bertahap (Feature-by-Feature)

[cite_start]*Aturan untuk AI: Untuk setiap fitur di fase ini, implementasikan dengan urutan: Domain Use Case -> Data Repository Implementation -> Presentation ViewModel & UI State -> Compose Screen.* [cite: 41]

### Step 3.1: Modul Onboarding (OB)
* [cite_start]**Tugas AI:** Implementasikan logika `isFirstLaunch` berbasis SharedPreferences/DataStore[cite: 25]. [cite_start]Bangun `Welcome Screen` dan `Guided Tour` (3 langkah) menggunakan Jetpack Compose[cite: 25].
* [cite_start]**Izin Perangkat:** Tulis alur *runtime request* untuk izin `POST_NOTIFICATIONS` di langkah terakhir onboarding[cite: 25, 34].
* [cite_start]**Output:** Onboarding berjalan lancar pada instalasi pertama dan langsung mengarah ke Dashboard pada peluncuran berikutnya[cite: 25].

### Step 3.2: Modul Tambah / Pencatatan Langganan (AS)
* [cite_start]**Tugas AI:** Buat form entri input data layanan, penentu harga/mata uang, date picker untuk siklus penagihan, dropdown metode pembayaran, kategori, dan toggle free trial[cite: 28].
* [cite_start]**Validasi Form:** Terapkan inline error state (misal: harga tidak boleh 0 atau kosong, nama maks 100 karakter)[cite: 28]. [cite_start]Implementasikan penyimpanan data hasil input ke Room DB via Use Case[cite: 28, 41].
* [cite_start]**Output:** Pengguna dapat menyimpan data langganan baru dan melihat konfirmasi Snackbar[cite: 21, 28].

### Step 3.3: Modul Dashboard Finansial (DB)
* [cite_start]**Tugas AI:** Bangun komponen ringkasan pengeluaran bulanan dalam IDR di bagian atas[cite: 31]. [cite_start]Buat `LazyColumn` (RecyclerView) untuk menampilkan daftar langganan aktif beserta badge dinamis ("Segera Jatuh Tempo" atau "Trial Berakhir")[cite: 28, 31].
* [cite_start]**Filter & Sorting:** Implementasikan logika penyaringan berdasarkan kategori/metode pembayaran serta pengurutan data[cite: 21, 31].
* [cite_start]**Output:** Antarmuka utama menampilkan data riil dari Room Database secara dinamis[cite: 31, 43].

### Step 3.4: Komponen Visualisasi Chart (Dashboard Lanjutan)
* [cite_start]**Tugas AI:** Tulis komponen kustom Donut Chart (untuk proporsi pengeluaran per kategori) dan Horizontal Bar Chart (untuk breakdown metode pembayaran) menggunakan Canvas Jetpack Compose[cite: 31].
* [cite_start]**Output:** Dashboard menampilkan visualisasi grafik yang interaktif saat data langganan $\ge 2$[cite: 31].

---

## FASE 4: Latar Belakang (Background Tasks) & Modul Analitik

### Step 4.1: Notifikasi Proaktif dengan WorkManager (NF)
* [cite_start]**Tugas AI:** Buat kelas `NotificationWorker` yang memperluas `CoroutineWorker`[cite: 34, 41]. [cite_start]Atur kueri Room untuk mendeteksi layanan yang jatuh tempo dalam 7 hari, 3 hari, dan hari-H[cite: 21, 34].
* [cite_start]**Penjadwalan:** Konfigurasikan `PeriodicWorkRequest` harian dengan kriteria batasan perangkat sedang *charging* atau *idle*[cite: 34]. [cite_start]Terapkan deduplikasi notifikasi agar tidak mengirim pesan ganda dalam 24 jam[cite: 34, 45].
* [cite_start]**Output:** Sistem pengingat latar belakang aktif terjadwal dan memicu push notification lokal secara berkala[cite: 34, 43].

### Step 4.2: Modul Rekapitulasi Biaya, Statistik, & Ghost Detector (RC)
* [cite_start]**Tugas AI:** Implementasikan kueri agregat data berdasarkan rentang waktu bulanan dan tahunan untuk menampilkan tren grafik garis[cite: 37].
* [cite_start]**Ghost Detector Logic:** Buat algoritma pemindai yang otomatis menandai langganan sebagai "Perlu Ditinjau" jika status pembayaran tidak dikonfirmasi selama 2 siklus berturut-turut[cite: 37].
* [cite_start]**Output:** Halaman laporan statistik berfungsi penuh dan mampu mendeteksi *ghost subscriptions*[cite: 21, 37].

### Step 4.3: Sistem Autentikasi & Manajemen Sesi (Auth)
* **Tugas AI:** Implementasikan integrasi Supabase Auth menggunakan Email & Password untuk alur Register dan Login.
* **Form Register:** Sediakan field Email, Password, dan Konfirmasi Password.
  * Wajib menerapkan fitur toggle visibility (Icon Eye Open / Eye Close) pada field Password dan Konfirmasi Password (secara default disembunyikan/*masked*).
  * Menyediakan button "Register" untuk melakukan konfirmasi pendaftaran user baru.
* **Form Login:** Sediakan field Email dan Password dengan visualisasi penyamaran password yang sama, serta button "Login".
* **Logika Alur Masuk (Sesi):**
  * **User Baru:** Wajib diarahkan melalui alur: `Modul Onboarding` -> `Register/Login Page` -> `Homepage`.
  * **User Lama (Sudah Pernah Login):** Aplikasi harus membaca sesi aktif Supabase yang tersimpan dan langsung melakukan *Bypass* masuk ke `Homepage` tanpa menampilkan Onboarding atau Login Page kembali.
* **Kebutuhan DB (query.md):** Tambahkan skrip SQL untuk mengaitkan setiap user baru yang terdaftar di `auth.users` secara otomatis dengan skema data kustom mereka (menggunakan PostgreSQL Trigger). Pastikan menggunakan sintaks `ALTER` atau `CREATE TABLE IF NOT EXISTS` secara kumulatif tanpa menimpa konfigurasi tabel `subscriptions` yang sudah ada.
* **Output:** Alur pendaftaran, masuk, penanganan keamanan enkripsi sandi, serta persistensi sesi multi-tenant Supabase berfungsi penuh.

---

## FASE 5: Perancangan Komponen UI Komprehensif & Struktur Navigasi Utama

### Step 5.1: Arsitektur Bottom Navigation Bar (Navbar Utama)
* **Tugas AI:** Rancang komponen Navbar di bagian bawah layar menggunakan Jetpack Compose dengan susunan 3 menu tetap:
  1. **Paling Kiri:** Icon Home (Mengarahkan ke `Homepage` / `DashboardScreen`).
  2. **Di Tengah:** Icon Plus (Mengarahkan ke Halaman `TambahLanggananScreen`).
  3. **Paling Kanan:** Icon Profile (Mengarahkan ke `ProfileScreen`).
* **Output:** Sistem navigasi antar-tab Scaffold berjalan mulus dengan mempertahankan status (*state*) masing-masing halaman.

### Step 5.2: Perombakan Struktur & Aksi UI Homepage (`DashboardScreen`)
* **Tugas AI:** Susun tata letak halaman beranda dari atas ke bawah secara presisi:
  1. **Header Profil:** Sisi kiri diisi Foto Profil User bersebelahan dengan teks Nama User. Sisi paling kanan diisi Button Notifikasi (Ikon Lonceng) untuk melihat seluruh log hasil notifikasi aktivitas pada aplikasi ini.
  2. **Card Analisis Keuangan (Full Width):** Mengisi lebar penuh layar untuk menampilkan visualisasi metrik pengeluaran finansial *Average Consumption* dari total biaya langganan user.
  3. **Card Daftar Aktif ("Active subscriptions"):** Menampilkan `LazyColumn` daftar item aplikasi yang sedang dilanggan oleh user:
    * **Komponen List Item:** Icon aplikasi (paling kiri), Nama Aplikasi (tengah atas), teks tanggal mulai penagihan berukuran lebih kecil (tengah bawah), Harga Langganan (sisi kanan), dan Ikon Chevron Right (paling kanan ujung). Setiap list item dapat diklik untuk membuka halaman Detail.
    * **Fitur Drag-to-Delete (Swipe Left):** Terapkan interaksi gestur seret ke kiri pada setiap item list. Saat diseret, munculkan tombol tempat sampah (`Trash`) dengan latar belakang berwarna merah solid (`bg-red`). Jika dilepas, hapus seluruh data langganan tersebut secara permanen dari Room DB dan cloud Supabase sehingga hilang dari daftar *Active Subscriptions*.
  4. **Card Riwayat Subscriptions:** Diletakkan di bagian paling bawah setelah daftar aktif untuk menampilkan daftar arsip/riwayat aplikasi yang pernah dilanggan sebelumnya oleh user.
* **Kebutuhan DB (query.md):** Tambahkan fungsi SQL kueri agregat `AVG()` untuk menghitung konsumsi rata-rata, serta kueri pembersihan/penghapusan (`DELETE`) data langganan yang dikaitkan secara ketat dengan `auth.uid()`.
* **Output:** Antarmuka beranda yang interaktif, informatif, dan mendukung manipulasi data responsif secara lokal maupun cloud.

### Step 5.3: Halaman Detail & Edit Langganan (`SubscriptionDetailScreen`)
* **Tugas AI:** Buat halaman detail interaktif yang terbuka ketika salah satu aplikasi di bawah *Active Subscriptions* diklik:
  * **Tampilan Data:** Merender secara informatif Icon App, Nama App, Total Biaya, Periode Subscriptions (*Billing Cycle*), dan Tanggal Mulai Penagihan.
  * **Logika Pengeditan Form:** Semua field di dalam detail ini dapat diedit oleh user (Biaya, Periode, dll), **KECUALI field Nama Aplikasi dan Icon Aplikasi yang dikunci otomatis (Read-Only)** karena sudah terbuat dari sistem bawaan.
  * **Fitur Akhiri Langganan:** Sediakan button "Akhiri Langganan" di bagian paling bawah halaman. Jika ditekan, perbarui status langganan di database sehingga item tersebut dikeluarkan/tidak ditampilkan lagi dari daftar *Active Subscriptions*.
* **Kebutuhan DB (query.md):** Tulis perintah SQL `UPDATE` spesifik untuk mengubah status langganan atau memperbarui detail tagihan tanpa mengubah nama aplikasi asli.
* **Output:** Kontrol penuh siklus hidup layanan langganan yang aman.

### Step 5.4: Modul Katalog Dinamis, Filter Kategori, & Tambah Langganan (`TambahLanggananScreen`)
* **Tugas AI:** Implementasikan alur di mana user tidak mengetik nama aplikasi secara manual dari nol, melainkan memilih katalog bawaan atau membuat entri kustom terisolasi:
  1. **Header Atas:** Teks judul "Tambah Langganan".
  2. **Search Field:** Input pencarian real-time untuk menyaring nama aplikasi secara spesifik.
  3. **Filter Bar Tabs Kategori:** Posisi tab paling kanan adalah opsi "All", diikuti oleh tab kategori default: *Popular*, *Cineman*, *Music*, dan *Social Network*. Tab berikutnya disesuaikan secara dinamis dengan kategori kustom yang dibuat pengguna.
  4. **Katalog & Kategori Kustom Per-User (Data Isolation):**
    * Berikan kemampuan bagi masing-masing user untuk menambahkan aplikasi baru (mengunggah icon kustom + nama aplikasi baru) atau membuat kategori baru yang belum ada di daftar default.
    * Aplikasi atau kategori baru tersebut dapat dikombinasikan (misalnya memasukkan aplikasi baru ke kategori default, atau aplikasi bawaan ke kategori baru).
    * **PENTING (Multi-Tenant Rule):** Entri data kustom ini hanya boleh tampil di aplikasi milik user yang membuatnya saja. User lain tidak boleh melihat aplikasi atau kategori kustom milik user lain.
  5. **Form Detail Berlangganan:** Saat salah satu aplikasi dari list katalog diklik, buka formulir pengisian data: Biaya Berlangganan, Metode Pembayaran, Siklus Penagihan (3 opsi dropdown/radio: *Weekly*, *Monthly*, *Yearly*), Tanggal Mulai Penagihan, Toggle Layanan *Free Trial*, dan button "Simpan Langganan".
* **Kebutuhan DB (query.md):** * Definisikan tabel baru kustomisasi untuk `user_categories` dan `user_apps` (jika belum ada).
  * **WAJIB** tambahkan kebijakan *Row Level Security* (RLS) PostgreSQL: `CREATE POLICY ... FOR SELECT USING (auth.uid() = user_id)` untuk memastikan isolasi data antar-user berjalan aman di server Supabase. Jangan hapus kueri tabel utama yang sudah ada sebelumnya di `query.md`.
* **Output:** Sistem manajemen katalog dinamis yang aman, terisolasi, dan terstruktur rapi.

### Step 5.5: Halaman Profil & Manajemen Pengguna (`ProfileScreen`)
* **Tugas AI:** Bangun halaman profil user dengan urutan komponen dari atas ke bawah:
  1. **Avatar & Identitas:** Foto Profil ditempatkan paling atas, diikuti teks Nama User, dan teks Email User di bawahnya.
  2. **List Item - Edit Profile:** Saat diklik, arahkan ke halaman edit untuk mengubah Foto Profil (melalui mekanisme upload file gambar dari galeri perangkat) dan Nama User. **Field Email terkunci permanen (Disabled/Read-Only)** dan tidak dapat diedit.
  3. **Logika Button Konfirmasi:** Jika user melakukan perubahan data profil, tampilkan button "Konfirmasi Perubahan" untuk menyimpan. Jika tidak ada perubahan yang dilakukan (atau setelah data berhasil disimpan), ubah teks button secara dinamis menjadi button "Kembali".
  4. **List Item - Logout:** Menyediakan tombol keluar akun yang akan menghapus seluruh data sesi aktif dari aplikasi, membersihkan penyimpanan lokal, dan mengarahkan paksa (*Pop Up To*) navigasi kembali ke halaman Login.
* **Kebutuhan DB (query.md):** Tambahkan skrip SQL `UPDATE` untuk tabel profil pengguna serta sinkronisasi penanganan URL file gambar yang diunggah ke Supabase Storage Bucket.
* **Output:** Modul manajemen akun mandiri pengguna yang andal dan aman.

---

## FASE 6: Polishing, Safe Area UI, & Validasi Kualitas Pengujian

### Step 6.1: Penanganan Edge-to-Edge & Safe Area UI
* **Tugas AI:** Sisipkan modifikator padding jendela sistem seperti `statusBarsPadding()`, `navigationBarsPadding()`, atau penanganan `contentPadding` dari komponen `Scaffold` Jetpack Compose pada seluruh file Screen. Pastikan elemen krusial seperti Navbar Bawah, komponen Header, dan floating fields tidak tertutup oleh lekukan layar (*notch*), punch-hole kamera, maupun navigasi gestur bawaan Android.
* **Output:** Tampilan aplikasi presisi, responsif di berbagai ukuran aspek rasio layar, dan mengikuti standar desain material modern.

### Step 6.2: Unit Testing Komponen Kritis & Aturan query.md
* **Tugas AI:** Tulis unit test di direktori `test/` untuk memvalidasi fungsi:
  1. Perhitungan kalkulasi finansial *Average Consumption*.
  2. Logika penyaringan filter tab kategori bawaan dan dinamis per-user.
  3. Validasi kebenaran input data (harga tidak boleh kosong/negatif).
* **Aturan File query.md:** Lakukan verifikasi akhir pada file `query.md`. Pastikan setiap skrip SQL baru (CRUD, RLS, Trigger, Constraint) ditulis di bagian bawah berkas secara teratur sebagai penambahan (*append*), tanpa menghapus atau menulis ulang informasi skema tabel subs yang sudah ada sejak awal.
* **Output:** Aplikasi stabil, minim bug, memiliki tingkat cakupan kode pengujian (*code coverage*) yang baik, serta skrip migrasi database server terarsip rapi.