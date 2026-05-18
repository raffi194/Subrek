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

-- 2. Persiapan Kebijakan Supabase Storage Bucket untuk Gambar Avatar (Jika Menggunakan Storage)
-- Mengizinkan pengguna mengunggah foto profil mereka sendiri ke dalam bucket bernama 'avatars'
CREATE POLICY "Pengguna dapat mengunggah avatar mereka sendiri" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Pengguna dapat memperbarui avatar mereka sendiri" ON storage.objects
    FOR UPDATE USING (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "Avatar dapat dilihat oleh publik" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');
