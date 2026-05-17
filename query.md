# SKEMA DATABASE & ROW LEVEL SECURITY (RLS) SUPABASE - SUBREK

Gunakan skema SQL di bawah ini langsung di SQL Editor Supabase Project Anda:
* **Project URL:** `https://gjnbqivjikulpjoovcme.supabase.co`
* **Anon Key:** Terlampir dalam konfigurasi lokal aplikasi

---

## 1. Pembuatan Tabel Subscriptions

```sql
CREATE TABLE public.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL CONSTRAINT price_check CHECK (price >= 0),
    currency VARCHAR(10) DEFAULT 'IDR' NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    next_payment_date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    is_trial BOOLEAN DEFAULT FALSE NOT NULL,
    is_ghost_subscription BOOLEAN DEFAULT FALSE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Mengaktifkan Row Level Security (RLS) untuk keamanan multi-tenant
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
```

## 2. Kebijakan Row Level Security (RLS) Kontrol Akses Data

```sql
-- 1. Kebijakan untuk operasi SELECT (Pengguna hanya bisa membaca data miliknya sendiri)
CREATE POLICY "Pengguna hanya dapat melihat langganan mereka sendiri."
ON public.subscriptions
FOR SELECT
USING (auth.uid() = user_id);

-- 2. Kebijakan untuk operasi INSERT (Pengguna hanya bisa memasukkan data dengan uid miliknya)
CREATE POLICY "Pengguna hanya dapat menambahkan langganan mereka sendiri."
ON public.subscriptions
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- 3. Kebijakan untuk operasi UPDATE (Pengguna hanya bisa memperbarui data miliknya sendiri)
CREATE POLICY "Pengguna hanya dapat memperbarui langganan mereka sendiri."
ON public.subscriptions
FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- 4. Kebijakan untuk operasi DELETE (Pengguna hanya bisa menghapus data miliknya sendiri)
CREATE POLICY "Pengguna hanya dapat menghapus langganan mereka sendiri."
ON public.subscriptions
FOR DELETE
USING (auth.uid() = user_id);
```

## 3. Trigger Otomatis untuk Kolom updated_at

```sql
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_subscriptions_modtime
    BEFORE UPDATE ON public.subscriptions
    FOR EACH ROW
    EXECUTE PROCEDURE update_modified_column();
```
