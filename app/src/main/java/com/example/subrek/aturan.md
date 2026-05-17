# SUBREK - Subscription Manager (Android Native)

Dokumen ini berfungsi sebagai instruksi utama, panduan arsitektur, dan referensi standar bagi AI/Developer untuk membangun aplikasi SUBREK. Seluruh pembuatan kode, struktur folder, dan implementasi fitur wajib mematuhi aturan yang tertulis di bawah ini.

---

## 1. Deskripsi & Tujuan Aplikasi
* **Nama Aplikasi:** SUBREK (Subscription Manager)
* **Platform:** Android Native (Minimum SDK 26 / Android 8.0+)
* **Tujuan:** Menjadi platform pencatatan dan pelacakan terpusat untuk seluruh siklus layanan digital pengguna. Aplikasi ini bertujuan mengembalikan kendali penuh pengguna atas pengeluaran pasif digital mereka melalui pencatatan terpusat, pengingat proaktif, dan transparansi finansial, serta mengatasi masalah *ghost subscriptions* dan *subscription fatigue*.

## 2. Stack Teknologi Utama
Aplikasi wajib dibangun menggunakan stack teknologi berikut tanpa terkecuali:
* **Bahasa Utama:** Kotlin (100% Modern Kotlin dengan Coroutines dan Flow)
* **UI Framework:** Jetpack Compose (Declarative UI)
* **Backend & Autentikasi:** Supabase (PostgreSQL, Realtime DB, dan Supabase Auth jika diperlukan di masa depan)
* **Dependency Injection:** Hilt (Google) untuk mengotomatiskan manajemen siklus hidup dependensi dan mempermudah unit testing.
* **Styling & Design Tokens:** Mengacu pada prinsip utilitas diadaptasi ke dalam `Theme.kt` Jetpack Compose menggunakan struktur penamaan design tokens yang serupa untuk flex, spacing, dan color palette.

## 3. Pola Arsitektur (Clean Architecture)
Aplikasi menerapkan **Clean Architecture** yang ketat dengan pemisahan 3 layer utama demi menjaga testabilitas dan skalabilitas:

### 3.1. Presentation Layer (UI & State)
* Berisi Jetpack Compose UI (Screens dan Components).
* **ViewModel (MVVM):** Mengelola UI State menggunakan `StateFlow` yang reaktif dan menerima input dari Domain Layer via Use Cases.

### 3.2. Domain Layer (Pure Kotlin Business Logic)
* **Entities:** Model data murni yang bebas dari dependensi framework/library pihak ketiga.
* **Use Cases (Interactors):** Representasi satu fungsi bisnis spesifik (misal: `GetActiveSubscriptionsUseCase`).
* **Repository Interfaces:** Kontrak data yang akan diimplementasikan oleh Data Layer.

### 3.3. Data Layer (Infrastructure & Data Sources)
* **Repository Implementations:** Menentukan kapan harus mengambil data dari lokal atau remote (Supabase).
* **Local Data Source:** Room Database (sebagai *Single Source of Truth* untuk mendukung kapabilitas *Offline-First*).
* **Remote Data Source:** Supabase Client API.

## 4. Struktur Folder (Directory Structure)
Struktur direktori di dalam `app/src/main/java/com/subrek/app/` diatur berdasarkan arsitektur berlapis (*feature-layered architecture*):

```text
com.subrek.app/
│
├── core/                           # Komponen global yang digunakan di banyak fitur
│   ├── theme/                      # Tema Jetpack Compose (Warna, Tipografi berbasis Tailwind v4.1)
│   ├── utils/                      # Helper, Extension, dan Common Class
│   └── di/                         # Global Hilt Modules
│
└── features/                       # Fitur-fitur aplikasi berdasarkan modul PRD
    ├── onboarding/                 # Modul Onboarding (Welcome, Guided Tour)
    ├── dashboard/                  # Modul Dashboard Finansial (Daftar & Ringkasan)
    ├── subscription/               # Modul Tambah/Edit & Pencatatan Langganan
    └── report/                     # Modul Rekapitulasi Biaya & Statistik
        ├── data/
        │   ├── local/              # Room Entity, DAO, LocalDataSource
        │   ├── remote/             # Supabase DTO, RemoteDataSource
        │   └── repository/         # Implementasi Repository
        ├── domain/
        │   ├── model/              # Domain Model (Entity)
        │   ├── repository/         # Interface Repository
        │   └── usecase/            # Use Case / Interactor
        └── presentation/
            ├── components/         # Komponen UI spesifik fitur (Reusable Widgets)
            ├── screens/            # Screen utama (Full Screen Composables)
            └── viewmodel/          # ViewModel & UI State