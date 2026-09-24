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

    const { data: { user }, error: authError } = await supabase.auth.getUser()
    if (authError || !user) {
    throw new Response(JSON.stringify({ error: 'Token inválido' }), { status: 401 })
    }

    const body = await req.json()

    let userType: number;
    if (body.user_type === "PROFESSOR") {
        userType = 1;
    } else if (body.user_type === "STUDENT") {
        userType = 2;
    }
    else {
        userType = -1;
    }    

    const user_profile_data = {
        user_profile_id: user.id,
        firstname: body.firstname,
        surname:body.surname,
        email: user.email,
        user_type: userType,
        is_full_profile: true,
        status: 1
    }

    const {data, error} = await supabase
    .from('user_profile')
    .insert([user_profile_data])
    .select('email,user_type,created_at')
    .single()

    if (error){
        throw new Response(JSON.stringify({ error: error.message }), { status: 401 })
    }

    return new Response(
      JSON.stringify({data: data}), 
      { status: 200, headers: { "Content-Type": "application/json" } }
    )

  } catch (err) {
    console.log(error)
    if (error instanceof Response) {
      return error;
    }
    return new Response(JSON.stringify({ error: error.message }), { status: 401 })
  }
})