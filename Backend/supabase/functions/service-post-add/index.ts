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

    if (courseCatalogIDs == null){
        return new Response(JSON.stringify({ code: "NO_COURSE_CATALOG",message:"No courses in catalog" }), { status: 404 })
    }

    // 2. Parse the request body
    const body = await req.json()

    // Construimos un objeto limpio de forma explícita
    const newPost = {
      author_id: user.id,
      author_type: user_type,
      author_name: user.user_metadata.name,
      course_catalog_id: body.course_catalog_id,
      title: body.title,
      content: body.content,
      is_private: body.is_private,
      allow_comments: body.allow_comments,
      email: user.email
    }

    // 3. Insert the record
    const {data:data, error: error } = await supabase
      .from('post') 
      .insert([newPost])
      .select('post_id,created_at')
      .single()

    if (error) {
      return new Response(
        JSON.stringify({success: false, error: error.message}), 
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      )
    }

    if (!data) {
        return new Response(
            JSON.stringify({ success: false, error: "No data returned" }),
            { status: 500, headers: { "Content-Type": "application/json" } }
        );
    }

    // Formatear created_at
    const createdAt = new Date(data.created_at)
    .toISOString()
    .replace('T', ' ')
    .substring(0, 19); // yyyy-MM-dd hh:mm:ss

    const postdata = {
        post_id : data.post_id,
        created_at: createdAt
    }
    // Retornamos la información personalizada de ese usuario
    return new Response(
      JSON.stringify({success: true, data: postdata}), 
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