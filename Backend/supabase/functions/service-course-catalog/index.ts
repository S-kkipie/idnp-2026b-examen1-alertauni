// Setup type definitions for built-in Supabase Runtime APIs
import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from 'jsr:@supabase/supabase-js@2'
import { getUserType, getCourseCatalogIDs } from "@shared/tools.ts"
import { requireFullProfile } from '@shared/auth.ts'

Deno.serve(async (req) => {
  try {
        const { userId, userEmail, userType, supabase } = await requireFullProfile(req)
        const courseCatalogIDs = await getCourseCatalogIDs(userType,userId,supabase)

        if (courseCatalogIDs == null){
            return new Response(JSON.stringify({ code: "NO_COURSE_CATALOG",message:"No courses in catalog" }), { status: 404 })
        }

        const { data, error } = await supabase.from('course_catalog_view')
        .select('course_catalog_id, course_code, course_name')
        .in(
            'course_catalog_id', courseCatalogIDs.map(c => c.course_catalog_id) // extraer los IDs del array
        )

        if (error) {
        throw error
        }

        return new Response(JSON.stringify({ data }), {
        headers: { 'Content-Type': 'application/json' },
        status: 200,
        })
    } catch (err) {
        return new Response(JSON.stringify({ message: err?.message ?? err }), {
        headers: { 'Content-Type': 'application/json' },
        status: 500 
        })
    }
})