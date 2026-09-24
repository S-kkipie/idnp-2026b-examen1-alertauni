// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { authProfile } from '@shared/auth2.ts'

serve(async (req: Request) => {
  try {
    
    // 1. Recuperamos los datos del usuario autenticado
    const { user, user_type, supabase } = await authProfile(req)

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
      is_private: true,
      receiver_private: body.receiver_private,
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
    console.error('Error detallado:', err.message)
    return new Response(JSON.stringify({ error: err.message }), { status: 400 })
  }
})