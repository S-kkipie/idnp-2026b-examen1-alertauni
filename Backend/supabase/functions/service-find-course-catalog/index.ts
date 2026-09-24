// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { getUserType, getCourseCatalogIDs } from "@shared/tools.ts"
import { requireFullProfile } from '@shared/auth.ts'

serve(async (req: Request) => {
    try {
        const { userId, userEmail, userType, supabase } = await requireFullProfile(req)
        const courseCatalogIDs = await getCourseCatalogIDs(userType,userId,supabase)

        if (courseCatalogIDs == null){
            return new Response(JSON.stringify({ code: "NO_COURSE_CATALOG",message:"No courses in catalog" }), { status: 404 })
        }

        const body = await req.json()

        const { data, error } = await supabase.from('student_enrollment_view')
        .select('course_catalog_id,course_id,course_code,course_name,semester,course_type,group_type,firstname,surname,email')
        .eq('class_code',body.class_code)
        .eq('class_code_enable',true)
        .single()

        if (error) throw error

        return new Response(
            JSON.stringify({data: data}), 
            { status: 200, headers: { "Content-Type": "application/json" } }
        )

    } catch (error) {
        // Si el error es la respuesta HTTP que lanzamos, la retornamos directo al cliente (Android)
        if (error instanceof Response) {
            return error;
        }
        // Por si ocurre otro tipo de error inesperado en el código
        return new Response(JSON.stringify({ error: error.message }), { status: 401 })
    }
})