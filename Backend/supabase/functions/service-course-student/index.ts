// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

serve(async (req: Request) => {
    try {

        const authHeader = req.headers.get('Authorization')
        if (!authHeader) {
            return new Response(JSON.stringify({ error: 'Falta token' }), { status: 401 })
        }

        const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ''
        const supabaseAnonKey = Deno.env.get('SUPABASE_ANON_KEY') ?? ''

        const supabase = createClient(supabaseUrl, supabaseAnonKey, {
            global: { headers: { Authorization: authHeader } }
        })

        const body = await req.json()
        const { data, error } = await supabase.from('student_course_view')
        .select('*')
        .eq('course_catalog_id',body.course_catalog_id)
        .order('surname', { ascending: true })

        console.log(data)
        console.log(error)
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