// Solicitudes de inscripción de estudiantes que no figuran en la lista oficial.
// Sólo el docente del curso. Acciones:
//   LIST    { course_catalog_id }            → solicitudes PENDING del curso
//   APPROVE { course_catalog_id, request_id } → inscribe al estudiante
//   REJECT  { course_catalog_id, request_id } → marca la solicitud como rechazada
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { requireFullProfile } from '@shared/auth.ts'

const PROFESSOR_TYPE = 1

function businessError(code: string, message: string, status = 404) {
    return new Response(JSON.stringify({ code, message }), {
        status, headers: { "Content-Type": "application/json" }
    })
}

function ok(data: unknown) {
    return new Response(JSON.stringify({ data }), {
        status: 200, headers: { "Content-Type": "application/json" }
    })
}

serve(async (req: Request) => {
    try {
        const { userId, userType, supabase } = await requireFullProfile(req)
        if (userType !== PROFESSOR_TYPE) {
            return businessError("ONLY_PROFESSORS", "Sólo el docente del curso puede revisar solicitudes", 401)
        }

        const body = await req.json()
        const courseCatalogId: string = body.course_catalog_id
        const action: string = body.action ?? "LIST"

        // El curso debe estar a cargo del docente que hace la petición
        const { data: course } = await supabase
            .from('course_catalog')
            .select('course_catalog_id')
            .eq('course_catalog_id', courseCatalogId)
            .eq('professor_id', userId)
            .maybeSingle()
        if (!course) {
            return businessError("COURSE_NOT_FOUND", "El curso no existe o no está a su cargo")
        }

        if (action === "LIST") {
            const { data, error } = await supabase
                .from('enrollment_request_view')
                .select('request_id, course_catalog_id, student_id, email, firstname, surname, created_at')
                .eq('course_catalog_id', courseCatalogId)
                .eq('status', 'PENDING')
                .order('created_at', { ascending: true })
            if (error) throw error
            return ok(data)
        }

        const requestId: number = body.request_id
        const { data: request } = await supabase
            .from('enrollment_request')
            .select('request_id, student_id, status')
            .eq('request_id', requestId)
            .eq('course_catalog_id', courseCatalogId)
            .maybeSingle()
        if (!request || request.status !== 'PENDING') {
            return businessError("REQUEST_NOT_FOUND", "La solicitud no existe o ya fue revisada")
        }

        if (action === "APPROVE") {
            const { error: enrollError } = await supabase
                .from('student_enrollment')
                .upsert(
                    [{ course_catalog_id: courseCatalogId, student_id: request.student_id }],
                    { onConflict: 'student_id,course_catalog_id', ignoreDuplicates: true }
                )
            if (enrollError) throw enrollError
        } else if (action !== "REJECT") {
            return businessError("INVALID_ACTION", "Acción no válida", 401)
        }

        const { error: updateError } = await supabase
            .from('enrollment_request')
            .update({
                status: action === "APPROVE" ? 'APPROVED' : 'REJECTED',
                reviewed_at: new Date().toISOString()
            })
            .eq('request_id', requestId)
        if (updateError) throw updateError

        return ok({ request_id: requestId, status: action === "APPROVE" ? 'APPROVED' : 'REJECTED' })
    } catch (err) {
        if (err instanceof Response) return err
        return new Response(JSON.stringify({ message: err?.message ?? String(err) }), {
            status: 500, headers: { "Content-Type": "application/json" }
        })
    }
})
