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
-- TAMBAHAN SECARA KUMULATIF UNTUK STEP 5.4 (KATALOG KUSTOM PER-USER & RLS)
-- =========================================================================

CREATE TABLE IF NOT EXISTS public.user_categories (
id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
name TEXT NOT NULL,
created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Mengaktifkan Row Level Security (RLS) untuk isolasi data antar-user
ALTER TABLE public.user_categories ENABLE ROW LEVEL SECURITY;

-- Kebijakan RLS agar user hanya bisa membaca & menulis data miliknya sendiri
CREATE POLICY "Allow individual read for owner" ON public.user_categories
FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Allow individual insert for owner" ON public.user_categories
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
