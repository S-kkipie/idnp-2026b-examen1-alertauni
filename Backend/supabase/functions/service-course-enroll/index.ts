// Incorporación de un estudiante a un curso (código escrito o QR escaneado).
// Respuestas de negocio con { code, message } y estado 404/401 para que la app
// las convierta en BackendException (ver common/Wrappers.kt).
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { requireFullProfile } from '@shared/auth.ts'

const STUDENT_TYPE = 2

function businessError(code: string, message: string, status = 404) {
    return new Response(JSON.stringify({ code, message }), {
        status, headers: { "Content-Type": "application/json" }
    })
}

serve(async (req: Request) => {
    try {
        const { userId, userEmail, userType, supabase } = await requireFullProfile(req)

        if (userType !== STUDENT_TYPE) {
            return businessError("ONLY_STUDENTS", "Sólo los estudiantes pueden inscribirse en un curso", 401)
        }

        const body = await req.json()
        const courseCatalogId: string = body.course_catalog_id

        // 1. El curso debe existir y tener la inscripción abierta y vigente
        const { data: course, error: courseError } = await supabase
            .from('course_catalog')
            .select('course_catalog_id, class_code_enable, class_code_expires_at')
            .eq('course_catalog_id', courseCatalogId)
            .single()

        if (courseError || !course) {
            return businessError("COURSE_NOT_FOUND", "El curso no existe")
        }
        const expired = course.class_code_expires_at &&
            new Date(course.class_code_expires_at).getTime() < Date.now()
        if (!course.class_code_enable || expired) {
            return businessError("ENROLLMENT_CLOSED", "La inscripción a este curso está cerrada o el código venció")
        }

        // 2. ¿Ya está inscrito?
        const { data: existing } = await supabase
            .from('student_enrollment')
            .select('student_id')
            .eq('course_catalog_id', courseCatalogId)
            .eq('student_id', userId)
            .maybeSingle()

        if (existing) {
            return businessError("ALREADY_ENROLLED", "Ya se encuentra registrado en este curso")
        }

        // 3. ¿Figura en la lista oficial de matriculados?
        const { data: rosterEntry } = await supabase
            .from('course_roster')
            .select('email')
            .eq('course_catalog_id', courseCatalogId)
            .ilike('email', userEmail)
            .maybeSingle()

        if (rosterEntry) {
            const { data, error } = await supabase
                .from('student_enrollment')
                .insert([{ course_catalog_id: courseCatalogId, student_id: userId }])
                .select('created_at')
                .single()
            if (error) throw error

            return new Response(
                JSON.stringify({ data: { created_at: data.created_at, status: "ENROLLED" } }),
                { status: 200, headers: { "Content-Type": "application/json" } }
            )
        }

        // 4. No figura: se registra (o reactiva) una solicitud pendiente para el docente
        const { data: request, error: requestError } = await supabase
            .from('enrollment_request')
            .upsert(
                [{ course_catalog_id: courseCatalogId, student_id: userId, email: userEmail, status: 'PENDING' }],
                { onConflict: 'course_catalog_id,student_id' }
            )
            .select('created_at')
            .single()
        if (requestError) throw requestError

        return new Response(
            JSON.stringify({ data: { created_at: request.created_at, status: "PENDING" } }),
            { status: 200, headers: { "Content-Type": "application/json" } }
        )
    } catch (err) {
        if (err instanceof Response) return err
        return new Response(JSON.stringify({ message: err?.message ?? String(err) }), {
            status: 500, headers: { "Content-Type": "application/json" }
        })
    }
})
