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
* [cite_start]**Tugas AI:** Tulis Room Entity (dengan anotasi `@Entity`), Data Access Object (DAO) dengan kueri CRUD dasar, dan kelas database utamanya[cite: 41, 43].
* [cite_start]**Output:** Local data source siap mengeksekusi operasi ke SQLite lokal secara aman[cite: 41, 43].

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

---

## FASE 5: Pemolesan (Polishing), Penanganan Safe Area, & Pengujian

### Step 5.1: Penanganan Edge-to-Edge UI & Safe Area
* **Tugas AI:** Periksa seluruh layar (`Screens`) dan pastikan elemen UI tidak tertutup notch atau system bar dengan menerapkan `statusBarsPadding()`, `navigationBarsPadding()`, atau penanganan padding `Scaffold` yang benar.
* [cite_start]**Output:** Tampilan aplikasi rapi, responsif di berbagai aspek rasio layar, dan mengikuti standar desain Android modern[cite: 45, 55].

### Step 5.2: Unit Testing & Pembersihan Kode
* [cite_start]**Tugas AI:** Buat unit test untuk komponen logika bisnis utama (Use Cases & ViewModel) di dalam direktori `test/` untuk memastikan target code coverage $\ge 70\%$ terpenuhi[cite: 45, 54].
* [cite_start]**Output:** Seluruh test suite lolos pengujian tanpa kegagalan (green builds)[cite: 45, 54].