-- Aktivasi ekstensi UUID
create extension if not exists "uuid-ossp";

-- Pembuatan Tabel Utama Subscriptions
create table if not exists public.subscriptions (
    id text primary key,
    user_id uuid references auth.users(id) on delete cascade not null,
    name text not null,
    price numeric(12, 2) not null default 0.00,
    currency text not null default 'IDR',
    billing_cycle text not null, -- WEEKLY, MONTHLY, YEARLY
    next_payment_date date not null,
    category text not null,
    payment_method text not null,
    status text not null, -- ACTIVE, TRIAL, PAUSED
    created_at timestamp with time zone default timezone('utc'::text, now()) not null,
    updated_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Aktifkan keamanan baris (RLS)
alter table public.subscriptions enable row level security;

-- Kebijakan Multi-Tenant (User hanya bisa akses datanya sendiri)
create policy "Akses data mandiri - SELECT" on public.subscriptions for select using (auth.uid() = user_id);
create policy "Akses data mandiri - INSERT" on public.subscriptions for insert with check (auth.uid() = user_id);
create policy "Akses data mandiri - UPDATE" on public.subscriptions for update using (auth.uid() = user_id) with check (auth.uid() = user_id);
create policy "Akses data mandiri - DELETE" on public.subscriptions for delete using (auth.uid() = user_id);

-- Trigger Otomatis Pembaruan stempel waktu updated_at
create or replace function public.update_modified_column()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger update_subscriptions_modtime
    before update on public.subscriptions
    for each row
    execute procedure public.update_modified_column();

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 5.4 (DYNAMIC CATALOG & MULTI-TENANT RLS)
-- =========================================================================

-- 1. Tabel Kategori Kustom per-User
CREATE TABLE IF NOT EXISTS public.user_categories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL,
    UNIQUE(user_id, name)
);

-- 2. Tabel Aplikasi Katalog Kustom per-User
CREATE TABLE IF NOT EXISTS public.user_apps (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name TEXT NOT NULL,
    icon_url TEXT,
    category_name TEXT NOT NULL, -- Dapat berupa kategori default atau kustom
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 3. Mengaktifkan Row Level Security (RLS) untuk keamanan isolasi data
ALTER TABLE public.user_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_apps ENABLE ROW LEVEL SECURITY;

-- 4. Kebijakan RLS untuk user_categories (Data Isolation)
CREATE POLICY "User hanya bisa melihat kategori kustom miliknya" ON public.user_categories
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "User hanya bisa menambah kategori kustom miliknya" ON public.user_categories
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- 5. Kebijakan RLS untuk user_apps (Data Isolation)
CREATE POLICY "User hanya bisa melihat aplikasi kustom miliknya" ON public.user_apps
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "User hanya bisa menambah aplikasi kustom miliknya" ON public.user_apps
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- =========================================================================
-- TAMBAHAN KUMULATIF UNTUK STEP 4.3 (SUPABASE AUTH & USER PROFILE TRIGGER)
-- =========================================================================

-- 1. Membuat tabel kustom profiles jika belum ada
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID REFERENCES auth.users(id) ON DELETE CASCADE PRIMARY KEY,
    full_name TEXT,
    avatar_url TEXT,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 2. Mengaktifkan Row Level Security (RLS) pada tabel profiles
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- 3. Kebijakan RLS untuk tabel profiles
CREATE POLICY "Allow users to read their own profile" ON public.profiles
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Allow users to update their own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);

-- 4. Fungsi trigger untuk menyisipkan data profil otomatis saat registrasi auth.users
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, avatar_url)
  VALUES (
    new.id, 
    COALESCE(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1)), 
    NULL
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  '8539b696-1c56-4dc7-9a09-de7f7f7a0f74', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  '8539b696-1c56-4dc7-9a09-de7f7f7a0f74', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  '8539b696-1c56-4dc7-9a09-de7f7f7a0f74', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   '8539b696-1c56-4dc7-9a09-de7f7f7a0f74', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     '8539b696-1c56-4dc7-9a09-de7f7f7a0f74', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

-- 5. Membuat trigger baru (pastikan di-drop dulu jika sudah ada agar aman)
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 5.2 (FINANCIAL AGGREGATES & HARD DELETE)
-- =========================================================================

-- Kueri Analisis Keuangan: Menghitung pengeluaran rata-rata bulanan user secara real-time
-- Catatan: Mengonversi siklus tagihan mingguan (weekly) dan tahunan (yearly) ke basis bulanan (monthly)
CREATE OR REPLACE VIEW public.user_subscription_analytics AS
SELECT 
    user_id,
    COALESCE(AVG(
        CASE 
            WHEN billing_cycle = 'WEEKLY' THEN price * 4.33
            WHEN billing_cycle = 'YEARLY' THEN price / 12.0
            ELSE price
        END
    ), 0) as average_monthly_consumption
FROM public.subscriptions
WHERE status = 'ACTIVE'
GROUP BY user_id;

-- Mengaktifkan pengamanan kueri agar fungsi penghapusan mematuhi aturan kepemilikan user
CREATE POLICY "Pengguna hanya dapat menghapus data langganan miliknya sendiri" ON public.subscriptions
    FOR DELETE USING (auth.uid() = user_id);

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 5.3 (MUTATION & BILLING LIFECYCLE)
-- =========================================================================

-- Kueri Pembaruan Detail Finansial: Mengedit parameter harga dan siklus tanpa mengubah metadata nama/ikon bawaan
CREATE OR REPLACE FUNCTION public.update_subscription_billing(
    p_subscription_id UUID,
    p_price NUMERIC,
    p_billing_cycle TEXT,
    p_start_date DATE
) RETURNS VOID AS $$
BEGIN
    UPDATE public.subscriptions
    SET 
        price = p_price,
        billing_cycle = p_billing_cycle,
        start_date = p_start_date,
        updated_at = NOW()
    WHERE id = p_subscription_id 
      AND user_id = auth.uid()
      AND status = 'ACTIVE';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

-- Kueri Akhiri Langganan: Mengubah status menjadi 'ENDED' agar dipindahkan secara otomatis ke Card Riwayat
CREATE OR REPLACE FUNCTION public.terminate_subscription_service(
    p_subscription_id UUID
) RETURNS VOID AS $$
BEGIN
    UPDATE public.subscriptions
    SET 
        status = 'ENDED',
        updated_at = NOW()
    WHERE id = p_subscription_id 
      AND user_id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 5.5 (USER PROFILE & STORAGE MANAGEMENT)
-- =========================================================================

-- 1. Kueri Fungsi Pembaruan Data Profil Pengguna
-- Catatan: Email sengaja dikunci dari sisi aplikasi (Read-only) dan tidak disertakan dalam fungsi pembaruan.
CREATE OR REPLACE FUNCTION public.handle_update_user_profile(
    target_user_id UUID,
    new_full_name TEXT,
    new_avatar_url TEXT
)
RETURNS VOID AS $$
BEGIN
    UPDATE public.profiles
    SET 
        full_name = new_full_name,
        avatar_url = new_avatar_url,
        updated_at = TIMEZONE('utc'::text, NOW())
    WHERE id = target_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

-- 2. Persiapan Kebijakan Supabase Storage Bucket untuk Gambar Avatar (Jika Menggunakan Storage)
-- Mengizinkan pengguna mengunggah foto profil mereka sendiri ke dalam bucket bernama 'avatars'
CREATE POLICY "Pengguna dapat mengunggah avatar mereka sendiri" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Pengguna dapat memperbarui avatar mereka sendiri" ON storage.objects
    FOR UPDATE USING (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Avatar dapat dilihat oleh publik" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF: MELENGKAPI KEKURANGAN CRUD & RLS FASE 5
-- =========================================================================

-- -------------------------------------------------------------------------
-- PERBAIKAN 1: Melengkapi Policy INSERT untuk Katalog Kustom (Step 5.4)
-- -------------------------------------------------------------------------
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'user_categories' AND policyname = 'User hanya bisa menambah kategori kustom miliknya'
    ) THEN
        CREATE POLICY "User hanya bisa menambah kategori kustom miliknya" 
        ON public.user_categories
        FOR INSERT WITH CHECK (auth.uid() = user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'user_apps' AND policyname = 'User hanya bisa menambah aplikasi kustom miliknya'
    ) THEN
        CREATE POLICY "User hanya bisa menambah aplikasi kustom miliknya" 
        ON public.user_apps
        FOR INSERT WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;


-- -------------------------------------------------------------------------
-- PERBAIKAN 2: Melengkapi Policy INSERT untuk Tabel Profiles (Step 5.5)
-- -------------------------------------------------------------------------
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'profiles' AND policyname = 'Pengguna dapat membuat profil sendiri'
    ) THEN
        CREATE POLICY "Pengguna dapat membuat profil sendiri" 
        ON public.profiles
        FOR INSERT WITH CHECK (auth.uid() = id);
    END IF;
END $$;


-- -------------------------------------------------------------------------
-- PERBAIKAN 3: Inisialisasi Tabel & CRUD Log Aktivitas Notifikasi (Step 5.2)
-- -------------------------------------------------------------------------

-- A. Membuat Tabel Log Aktivitas Notifikasi jika belum ada
CREATE TABLE IF NOT EXISTS public.notification_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    activity_type TEXT NOT NULL, -- 'PAYMENT_REMINDER', 'SUBS_TERMINATED', 'COST_ALERT'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- B. Mengaktifkan Row Level Security (RLS)
ALTER TABLE public.notification_logs ENABLE ROW LEVEL SECURITY;

-- C. CRUD - READ: Policy agar user hanya bisa membaca log miliknya sendiri
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'notification_logs' AND policyname = 'User hanya bisa melihat log aktivitas sendiri'
    ) THEN
        CREATE POLICY "User hanya bisa melihat log aktivitas sendiri" 
        ON public.notification_logs
        FOR SELECT USING (auth.uid() = user_id);
    END IF;
END $$;

-- D. CRUD - CREATE: Policy agar sistem/user bisa mencatat log aktivitas baru
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'notification_logs' AND policyname = 'User/Sistem bisa mencatat log aktivitas baru'
    ) THEN
        CREATE POLICY "User/Sistem bisa mencatat log aktivitas baru" 
        ON public.notification_logs
        FOR INSERT WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;

-- E. CRUD - DELETE: Policy agar user bisa membersihkan/menghapus log aktivitas
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'notification_logs' AND policyname = 'User bisa menghapus log aktivitas sendiri'
    ) THEN
        CREATE POLICY "User bisa menghapus log aktivitas sendiri" 
        ON public.notification_logs
        FOR DELETE USING (auth.uid() = user_id);
    END IF;
END $$;

-- =========================================================================
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 6.2 (DATABASE CONSTRAINTS VERIFICATION)
-- =========================================================================

-- Menambahkan check constraint pada tabel subscriptions untuk memvalidasi harga di level database
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_price_non_negative'
    ) THEN
        ALTER TABLE public.subscriptions 
        ADD CONSTRAINT check_price_non_negative CHECK (price >= 0);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'check_user_apps_name_not_empty'
    ) THEN
        ALTER TABLE public.user_apps 
        ADD CONSTRAINT check_user_apps_name_not_empty CHECK (length(trim(name)) > 0);
    END IF;
END $$;

-- =========================================================================
-- SINKRONISASI AKHIR KUMULATIF: MEMASTIKAN KESUKSESAN MULTI-TENANT & CRUD 100%
-- =========================================================================

-- 1. Inisialisasi Tabel Riwayat Log Aktivitas Notifikasi (Mendukung Tombol Lonceng Homepage)
CREATE TABLE IF NOT EXISTS public.notification_logs (
id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
title TEXT NOT NULL,
message TEXT NOT NULL,
activity_type TEXT NOT NULL, -- 'PAYMENT_REMINDER', 'SUBS_TERMINATED', 'COST_ALERT'
created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- 2. Mengaktifkan Row Level Security (RLS) pada Tabel Log Aktivitas
ALTER TABLE public.notification_logs ENABLE ROW LEVEL SECURITY;

-- 3. Kebijakan Keamanan RLS Multi-Tenant Terisolasi (Mencegah Kebocoran Data Antar-User)
DO $$
BEGIN
IF NOT EXISTS (
SELECT 1 FROM pg_policies
WHERE tablename = 'notification_logs' AND policyname = 'User hanya bisa membaca log miliknya sendiri'
) THEN
CREATE POLICY "User hanya bisa membaca log miliknya sendiri"
ON public.notification_logs
FOR SELECT USING (auth.uid() = user_id);
END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'notification_logs' AND policyname = 'User bisa menghapus log aktivitas sendiri'
    ) THEN
        CREATE POLICY "User bisa menghapus log aktivitas sendiri" 
        ON public.notification_logs
        FOR DELETE USING (auth.uid() = user_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'notification_logs' AND policyname = 'Sistem bisa mencatat aktivitas baru'
    ) THEN
        CREATE POLICY "Sistem bisa mencatat aktivitas baru" 
        ON public.notification_logs
        FOR INSERT WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;

-- 4. Validasi Pengaman Integritas Nilai Finansial (Fase 6.2)
DO $$
BEGIN
IF NOT EXISTS (
SELECT 1 FROM pg_constraint WHERE conname = 'check_price_non_negative'
) THEN
ALTER TABLE public.subscriptions
ADD CONSTRAINT check_price_non_negative CHECK (price >= 0);
END IF;
END $$;

-- =========================================================================
-- TAMBAHAN: SUPABASE AUTH - UBAH PASSWORD (CHANGE PASSWORD)
-- =========================================================================
-- Catatan: Ubah password dilakukan via Supabase Auth SDK (supabaseClient.auth.updateUser)
-- dari sisi Android client. Tidak memerlukan fungsi SQL khusus karena Supabase Auth
-- menangani enkripsi password secara otomatis di sisi server.
-- Fungsi di bawah ini hanya sebagai referensi audit log perubahan profil.

CREATE OR REPLACE FUNCTION public.log_password_change(target_user_id UUID)
RETURNS VOID AS $$
BEGIN
    INSERT INTO public.notification_logs (user_id, title, message, activity_type)
    VALUES (
        target_user_id,
        'Password Diubah',
        'Password akun Anda berhasil diperbarui.',
        'SECURITY_ALERT'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

-- =========================================================================
-- TAMBAHAN KUMULATIF: STEP 5.2 SWIPE-TO-DELETE & HISTORY (Gap Fix)
-- =========================================================================

-- Memastikan policy DELETE pada subscriptions sudah ada (idempoten)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'subscriptions'
          AND policyname = 'Pengguna hanya dapat menghapus data langganan miliknya sendiri'
    ) THEN
        CREATE POLICY "Pengguna hanya dapat menghapus data langganan miliknya sendiri"
        ON public.subscriptions
        FOR DELETE USING (auth.uid() = user_id);
    END IF;
END $$;

-- View untuk mengambil riwayat langganan yang sudah berakhir (status = 'ENDED')
-- Digunakan oleh Card Riwayat Subscriptions di DashboardScreen
CREATE OR REPLACE VIEW public.user_subscription_history AS
SELECT
    id,
    user_id,
    name,
    price,
    currency,
    billing_cycle,
    category,
    payment_method,
    status,
    next_payment_date,
    created_at,
    updated_at
FROM public.subscriptions
WHERE status = 'ENDED';

-- =========================================================================
-- TAMBAHAN KUMULATIF: STEP 5.4 SYNC KATALOG KUSTOM KE SUPABASE (Gap Fix)
-- =========================================================================

-- Memastikan kolom 'id' pada user_categories menerima tipe text (UUID dari Android)
-- Android menggunakan UUID.randomUUID().toString() yang menghasilkan tipe text/varchar
ALTER TABLE public.user_categories
    ALTER COLUMN id TYPE TEXT;

ALTER TABLE public.user_apps
    ALTER COLUMN id TYPE TEXT;

-- Policy UPDATE untuk user_categories (untuk upsert dari Android client)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'user_categories'
          AND policyname = 'User bisa memperbarui kategori kustom miliknya'
    ) THEN
        CREATE POLICY "User bisa memperbarui kategori kustom miliknya"
        ON public.user_categories
        FOR UPDATE USING (auth.uid() = user_id)
        WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;

-- Policy UPDATE untuk user_apps (untuk upsert dari Android client)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'user_apps'
          AND policyname = 'User bisa memperbarui aplikasi kustom miliknya'
    ) THEN
        CREATE POLICY "User bisa memperbarui aplikasi kustom miliknya"
        ON public.user_apps
        FOR UPDATE USING (auth.uid() = user_id)
        WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;

-- Policy DELETE untuk user_categories (jika user ingin menghapus kategori kustom)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'user_categories'
          AND policyname = 'User bisa menghapus kategori kustom miliknya'
    ) THEN
        CREATE POLICY "User bisa menghapus kategori kustom miliknya"
        ON public.user_categories
        FOR DELETE USING (auth.uid() = user_id);
    END IF;
END $$;

-- Policy DELETE untuk user_apps (jika user ingin menghapus app kustom)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies
        WHERE tablename = 'user_apps'
          AND policyname = 'User bisa menghapus aplikasi kustom miliknya'
    ) THEN
        CREATE POLICY "User bisa menghapus aplikasi kustom miliknya"
        ON public.user_apps
        FOR DELETE USING (auth.uid() = user_id);
    END IF;
END $$;

-- Fungsi helper untuk sync batch katalog kustom dari Android ke Supabase
-- Dipanggil saat online setelah insert lokal berhasil
CREATE OR REPLACE FUNCTION public.upsert_user_category(
    p_id TEXT,
    p_name TEXT
) RETURNS VOID AS $$
BEGIN
    INSERT INTO public.user_categories (id, user_id, name)
    VALUES (p_id, auth.uid(), p_name)
    ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/

CREATE OR REPLACE FUNCTION public.upsert_user_app(
    p_id TEXT,
    p_name TEXT,
    p_icon_url TEXT,
    p_category_name TEXT
) RETURNS VOID AS $$
BEGIN
    INSERT INTO public.user_apps (id, user_id, name, icon_url, category_name)
    VALUES (p_id, auth.uid(), p_name, p_icon_url, p_category_name)
    ON CONFLICT (id) DO UPDATE
        SET name = EXCLUDED.name,
            icon_url = EXCLUDED.icon_url,
            category_name = EXCLUDED.category_name;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =========================================================================
-- SEED DATA DEFAULT (Jalankan sekali setelah setup awal database)
-- Data ini menjadi katalog bawaan aplikasi SUBREK
-- =========================================================================

-- Catatan: Data seed di bawah menggunakan user_id fiktif untuk referensi.
-- Di production, katalog app bawaan dikelola dari sisi Android Room DB (offline-first).
-- Tabel user_categories dan user_apps membutuhkan user_id valid dari auth.users.
-- Seed ini hanya untuk keperluan testing/development environment.

-- INSERT CONTOH DATA SUBSCRIPTIONS (Untuk Testing di Supabase Dashboard)
-- Ganti 'YOUR_TEST_USER_UUID' dengan UUID user yang sudah terdaftar di auth.users
/*
INSERT INTO public.subscriptions 
    (id, user_id, name, price, currency, billing_cycle, next_payment_date, category, payment_method, status, created_at)
VALUES
    ('demo_netflix',  'YOUR_TEST_USER_UUID', 'Netflix',          54000,  'IDR', 'MONTHLY', NOW() + INTERVAL '7 days',  'Cineman',      'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '3 months'),
    ('demo_spotify',  'YOUR_TEST_USER_UUID', 'Spotify',          54990,  'IDR', 'MONTHLY', NOW() + INTERVAL '14 days', 'Music',         'E-Wallet',     'ACTIVE', NOW() - INTERVAL '5 months'),
    ('demo_youtube',  'YOUR_TEST_USER_UUID', 'YouTube Premium',  59000,  'IDR', 'MONTHLY', NOW() + INTERVAL '3 days',  'Popular',       'Kartu Kredit', 'ACTIVE', NOW() - INTERVAL '1 month'),
    ('demo_notion',   'YOUR_TEST_USER_UUID', 'Notion',          160000,  'IDR', 'YEARLY',  NOW() + INTERVAL '30 days', 'Productivity',  'Transfer Bank','TRIAL',  NOW() - INTERVAL '2 months'),
    ('demo_mola',     'YOUR_TEST_USER_UUID', 'Mola TV',          39000,  'IDR', 'MONTHLY', NOW() - INTERVAL '1 month', 'Cineman',       'E-Wallet',     'ENDED',  NOW() - INTERVAL '1 year')
ON CONFLICT (id) DO NOTHING;
*/
