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
