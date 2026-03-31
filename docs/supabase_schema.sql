-- Extensión para UUID en Supabase/Postgres
create extension if not exists "uuid-ossp";

-- TABLA CLIENTES
create table if not exists clientes (
  id uuid primary key default uuid_generate_v4(),
  nombre text,
  telefono text,
  ubicacion text,
  created_at timestamp default now()
);

-- TABLA PRODUCTOS
create table if not exists productos (
  id uuid primary key default uuid_generate_v4(),
  nombre text,
  precio numeric,
  imagen_url text,
  created_at timestamp default now()
);

-- TABLA PROMOCIONES
create table if not exists promociones (
  id uuid primary key default uuid_generate_v4(),
  titulo text,
  imagen_url text,
  precio_normal numeric,
  precio_promo numeric,
  created_at timestamp default now()
);

-- RELACIÓN PROMOCION-PRODUCTOS
create table if not exists promocion_productos (
  id uuid primary key default uuid_generate_v4(),
  promocion_id uuid references promociones(id),
  nombre text,
  precio numeric
);

-- Recomendado: activar RLS y crear políticas según seguridad de tu app
alter table clientes enable row level security;
alter table productos enable row level security;
alter table promociones enable row level security;
alter table promocion_productos enable row level security;
