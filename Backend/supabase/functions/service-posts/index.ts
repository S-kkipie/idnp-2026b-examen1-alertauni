// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { getUserType, getCourseCatalogIDs } from "@shared/tools.ts"
import { authProfile } from '@shared/auth2.ts'

serve(async (req: Request) => {
    try {
        //const { userId, userEmail, userType, supabase } = await requireFullProfile(req)
        const { user, user_type, supabase } = await authProfile(req)
        const courseCatalogIDs = await getCourseCatalogIDs(user_type,user.id,supabase)

        if (courseCatalogIDs == null){
            return new Response(JSON.stringify({ code: "NO_COURSE_CATALOG",message:"No courses in catalog" }), { status: 404 })
        }

        // const { data, error } = await supabase.from('posts_view')
        // .select('*')
        // .in(
        // 'course_catalog_id', courseCatalogIDs.map(c => c.course_catalog_id) // extraer los IDs del array
        // )
        // .order('id', { ascending: false })

        const courseIds = courseCatalogIDs.map(c => c.course_catalog_id)

        const { data, error } = await supabase
        .rpc('get_posts_fun',{
            course_ids: courseIds,
            receiver_sender: user.id            
        })

        // console.log(data)
        // console.log(error)
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