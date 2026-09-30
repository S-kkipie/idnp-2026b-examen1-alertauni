-- Proyecto 01 · Incorporación de estudiantes a un curso
-- Mecanismo híbrido: código/QR (mismo token) + validación contra la lista oficial
-- de matriculados; quien no figura queda PENDIENTE de aprobación del docente.

-- 1. Vencimiento del código de clase (class_code y class_code_enable ya existen)
alter table public.course_catalog
    add column if not exists class_code_expires_at timestamptz;

create unique index if not exists course_catalog_class_code_uk
    on public.course_catalog (class_code)
    where class_code is not null;

-- 2. Lista oficial de matriculados (importada por el docente o la escuela)
create table if not exists public.course_roster (
    course_catalog_id text not null references public.course_catalog (course_catalog_id) on delete cascade,
    email             text not null,
    codigo            text,
    created_at        timestamptz not null default now(),
    primary key (course_catalog_id, email)
);

-- 3. Solicitudes de estudiantes que no figuran en la lista oficial
create table if not exists public.enrollment_request (
    request_id        bigint generated always as identity primary key,
    course_catalog_id text not null references public.course_catalog (course_catalog_id) on delete cascade,
    student_id        uuid not null,
    email             text not null,
    status            text not null default 'PENDING'
                      check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    created_at        timestamptz not null default now(),
    reviewed_at       timestamptz,
    unique (course_catalog_id, student_id)
);

-- 4. Seguridad a nivel de fila: cada estudiante ve sólo sus solicitudes y
--    el docente sólo las de sus cursos.
alter table public.course_roster enable row level security;
alter table public.enrollment_request enable row level security;

create policy "docente gestiona la lista de su curso" on public.course_roster
    for all using (
        exists (select 1 from public.course_catalog c
                where c.course_catalog_id = course_roster.course_catalog_id
                  and c.professor_id = auth.uid())
    );

create policy "estudiante ve sus solicitudes" on public.enrollment_request
    for select using (student_id = auth.uid());

create policy "estudiante crea su solicitud" on public.enrollment_request
    for insert with check (student_id = auth.uid());

create policy "docente revisa solicitudes de su curso" on public.enrollment_request
    for all using (
        exists (select 1 from public.course_catalog c
                where c.course_catalog_id = enrollment_request.course_catalog_id
                  and c.professor_id = auth.uid())
    );

-- 5. Vista para que el docente vea el nombre de quien solicita
--    (security_invoker: respeta las políticas RLS de quien consulta)
create or replace view public.enrollment_request_view
with (security_invoker = true) as
select r.request_id,
       r.course_catalog_id,
       r.student_id,
       r.email,
       p.firstname,
       p.surname,
       r.status,
       r.created_at
from public.enrollment_request r
left join public.user_profile p on p.user_profile_id = r.student_id;
