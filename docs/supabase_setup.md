# Supabase setup (Nexora)

## 1) Activar REST API
En Supabase, la REST API de PostgREST ya viene habilitada por defecto.
- Ve a **Project Settings > API** para ver la URL base y keys.
- Base REST: `https://<PROJECT_REF>.supabase.co/rest/v1/`

## 2) Obtener URL y API KEY
1. Entra a **Project Settings > API**.
2. Copia:
   - `Project URL` → usar como `SUPABASE_URL`
   - `anon public key` → usar como `SUPABASE_KEY`
3. En `local.properties` del proyecto Android:
   ```properties
   SUPABASE_URL=https://<PROJECT_REF>.supabase.co
   SUPABASE_KEY=<TU_ANON_KEY>
   ```

## 3) Crear Storage bucket para promos
1. Ve a **Storage**.
2. Crea bucket llamado `promos`.
3. Si quieres URL pública directa, marca bucket como **Public**.

## 4) Ejecutar SQL
Ejecuta `docs/supabase_schema.sql` en el SQL editor de Supabase.

## 5) Flujo usado por app
La app hace:
1. Genera imagen PNG de promoción en local.
2. Sube la imagen a `storage/v1/object/promos/...`.
3. Obtiene URL pública (`/storage/v1/object/public/promos/...`).
4. Inserta promoción en tabla `promociones`.
5. Inserta productos en tabla `promocion_productos`.
6. Genera link web: `https://tudominio.com?id={promo_id}`.

## 6) Despliegue web (Netlify o Vercel)
- Sube `index.html` como sitio estático.
- Obtén URL pública (ej: `https://nexora-promos.netlify.app`).
- En app usa ese dominio como base para generar el link.

## 7) Nota de seguridad
Para producción, usa RLS + JWT de usuario autenticado.
Evita usar service_role en cliente móvil.
