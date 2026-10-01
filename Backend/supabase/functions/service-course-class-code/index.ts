// Código de inscripción de un curso (sólo su docente).
// Acciones: GET | REGENERATE | OPEN | CLOSE
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { requireFullProfile } from '@shared/auth.ts'

const PROFESSOR_TYPE = 1
const CODE_VALIDITY_DAYS = 7
// Sin 0/O ni 1/I/L para evitar confusiones al dictar o escribir el código
const ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

function randomSuffix(length = 4): string {
    const bytes = crypto.getRandomValues(new Uint8Array(length))
    return Array.from(bytes, b => ALPHABET[b % ALPHABET.length]).join('')
}

function businessError(code: string, message: string, status = 404) {
    return new Response(JSON.stringify({ code, message }), {
        status, headers: { "Content-Type": "application/json" }
    })
}

serve(async (req: Request) => {
    try {
        const { userId, userType, supabase } = await requireFullProfile(req)
        if (userType !== PROFESSOR_TYPE) {
            return businessError("ONLY_PROFESSORS", "Sólo el docente del curso puede ver su código", 401)
        }

        const body = await req.json()
        const courseCatalogId: string = body.course_catalog_id
        const action: string = body.action ?? "GET"

        const { data: course, error } = await supabase
            .from('course_catalog_view')
            .select('course_catalog_id, course_code, professor_id, class_code, class_code_enable, class_code_expires_at')
            .eq('course_catalog_id', courseCatalogId)
            .single()

        if (error || !course || course.professor_id !== userId) {
            return businessError("COURSE_NOT_FOUND", "El curso no existe o no está a su cargo")
        }

        let changes: Record<string, unknown> = {}
        if (action === "REGENERATE" || (action === "GET" && !course.class_code)) {
            const expiresAt = new Date(Date.now() + CODE_VALIDITY_DAYS * 24 * 3600 * 1000)
            changes = {
                class_code: `${course.course_code}-${randomSuffix()}`.toUpperCase(),
                class_code_enable: true,
                class_code_expires_at: expiresAt.toISOString()
            }
        } else if (action === "OPEN") {
            changes = { class_code_enable: true }
        } else if (action === "CLOSE") {
            changes = { class_code_enable: false }
        }

        let current = course
        if (Object.keys(changes).length > 0) {
            const { data: updated, error: updateError } = await supabase
                .from('course_catalog')
                .update(changes)
                .eq('course_catalog_id', courseCatalogId)
                .select('class_code, class_code_enable, class_code_expires_at')
                .single()
            if (updateError) throw updateError
            current = { ...course, ...updated }
        }

        const count = async (table: string, extra?: (q: any) => any) => {
            let query = supabase.from(table).select('*', { count: 'exact', head: true })
                .eq('course_catalog_id', courseCatalogId)
            if (extra) query = extra(query)
            const { count } = await query
            return count ?? 0
        }

        const data = {
            course_catalog_id: courseCatalogId,
            class_code: current.class_code,
            class_code_enable: current.class_code_enable,
            class_code_expires_at: current.class_code_expires_at?.substring(0, 10) ?? null,
            enrolled_count: await count('student_enrollment'),
            roster_count: await count('course_roster'),
            pending_count: await count('enrollment_request', q => q.eq('status', 'PENDING'))
        }

        return new Response(JSON.stringify({ data }), {
            status: 200, headers: { "Content-Type": "application/json" }
        })
    } catch (err) {
        if (err instanceof Response) return err
        return new Response(JSON.stringify({ message: err?.message ?? String(err) }), {
            status: 500, headers: { "Content-Type": "application/json" }
        })
    }
})
