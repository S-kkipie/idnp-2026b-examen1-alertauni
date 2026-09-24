// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { authProfile } from '@shared/auth2.ts'

serve(async (req: Request) => {
  try {
    // const authHeader = req.headers.get('Authorization')
    // if (!authHeader) {
    //   return new Response(JSON.stringify({ error: "No autorizado" }), { status: 401 })
    // }

    // // Inicializamos el cliente con el token del usuario
    // const supabase = createClient(
    //   Deno.env.get('SUPABASE_URL')!,
    //   Deno.env.get('SUPABASE_ANON_KEY')!,
    //   { global: { headers: { Authorization: authHeader } } }
    // )

    // // 1. Recuperamos los datos del usuario autenticado
    // const { data: { user }, error: authError } = await supabase.auth.getUser()
    
    // if (authError || !user) {
    //   return new Response(JSON.stringify({ error: "Sesión inválida o expirada" }), { status: 401 })
    // }

    // // 2. ¡Aquí tienes los datos de su cuenta de Google!
    // const userId = user.id       // El UUID único del usuario (ej: "d3b07384d...")
    // const userEmail = user.email // El correo de Google (ej: "usuario@gmail.com")
    // const userName = user.user_metadata.name    
    // 1. Recuperamos los datos del usuario autenticado
    const { user, user_type, supabase } = await authProfile(req)
    const body = await req.json()

    const newComment = {
      post_id: body.post_id,
      author_id: user.id,
      author_type: user_type,
      content: body.content,
      is_private: body.is_private,
      author_name: user.user_metadata.name,
      email: user.email
    }

     // 3. Insert the record
    const {error } = await supabase
      .from('comment') 
      .insert([newComment])

    if (error) {
      console.error('Error:', error)
      console.error('Message:', error.message)
      console.error('Code:', error.code)
      console.error('Details:', error.details)

      return new Response(
        JSON.stringify({ error: error.message, details: error.details }), 
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      )
    }

    // Retornamos la información personalizada de ese usuario
    return new Response(
      JSON.stringify({success: true}), 
      { status: 200, headers: { "Content-Type": "application/json" } }
    )

  } catch (err) {
        // Si el error es la respuesta HTTP que lanzamos, la retornamos directo al cliente (Android)
        if (error instanceof Response) {
        return error;
        }
        // Por si ocurre otro tipo de error inesperado en el código
        return new Response(JSON.stringify({ error: error.message }), { status: 401 })
  }
})