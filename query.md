-- 1. Mengaktifkan Ekstensi UUID jika diperlukan
create extension if not exists "uuid-ossp";

-- 2. Pembuatan Tabel Subscriptions (Sesuai dengan Atribut SubscriptionDto lokal)
create table if not exists public.subscriptions (
    id text primary key, -- Menggunakan UUID string dari client Android
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

-- 3. Mengaktifkan Row Level Security (RLS) demi keamanan multi-tenant
alter table public.subscriptions enable row level security;

-- 4. Pembuatan Kebijakan Keamanan (RLS Policies)
create policy "Pengaruh data mandiri pengguna - Select" 
on public.subscriptions for select 
using (auth.uid() = user_id);

create policy "Pengaruh data mandiri pengguna - Insert" 
on public.subscriptions for insert 
with check (auth.uid() = user_id);

create policy "Pengaruh data mandiri pengguna - Update" 
on public.subscriptions for update 
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "Pengaruh data mandiri pengguna - Delete" 
on public.subscriptions for delete 
using (auth.uid() = user_id);

-- 5. Trigger Otomatis untuk memperbarui kolom updated_at server
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
