// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

serve(async (req: Request) => {
  try {
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
      return new Response(JSON.stringify({ error: "No autorizado" }), { status: 401 })
    }

    // Inicializamos el cliente con el token del usuario
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL')!,
      Deno.env.get('SUPABASE_ANON_KEY')!,
      { global: { headers: { Authorization: authHeader } } }
    )

    // 1. Recuperamos los datos del usuario autenticado
    const { data: { user }, error: authError } = await supabase.auth.getUser()
    
    if (authError || !user) {
      return new Response(JSON.stringify({ error: "Sesión inválida o expirada" }), { status: 401 })
    }

    // 2. ¡Aquí tienes los datos de su cuenta de Google!
    const userId = user.id       // El UUID único del usuario (ej: "d3b07384d...")
    const userEmail = user.email // El correo de Google (ej: "usuario@gmail.com")
    //const userName = user.user_metadata.name
    
    // 2. Parse the request body
    const body = await req.json()

    // Construimos un objeto limpio de forma explícita
    const userAccount = {
        user_profile_id: userId,
        firstname: body.firstname,
        surname: body.surname,
        user_type: body.user_type,
        email: userEmail,
        status: 1 //full profile
    }

    // 3. Insert the record
    const {dbError } = await supabase
      .from('user_profile') 
      .insert([userAccount])

    if (dbError) {
      console.error('Error detallado:', dbError)
      console.error('Mensaje:', dbError.message)
      console.error('Código:', dbError.code)
      console.error('Detalles:', dbError.details)

      return new Response(
        JSON.stringify({ error: dbError.message, details: dbError.details }), 
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      )
    }

    // Retornamos la información personalizada de ese usuario
    return new Response(
      JSON.stringify({success: true}), 
      { status: 200, headers: { "Content-Type": "application/json" } }
    )

  } catch (err) {
    console.error('Error detallado:', err.message)
    return new Response(JSON.stringify({ error: err.message }), { status: 400 })
  }
})