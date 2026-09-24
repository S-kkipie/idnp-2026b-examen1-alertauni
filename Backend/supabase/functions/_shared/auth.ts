import { createClient, SupabaseClient } from 'https://esm.sh/@supabase/supabase-js@2'

interface AuthResult {
  userId: string;
  userEmail: string;
  userType: number;
  supabase: SupabaseClient;
}

export async function requireFullProfile(req: Request): Promise<AuthResult> {
    const authHeader = req.headers.get('Authorization')
    if (!authHeader) {
        return new Response(JSON.stringify({ error: 'Falta token' }), { status: 401 })
    }

    const supabaseUrl = Deno.env.get('SUPABASE_URL') ?? ''
    const supabaseAnonKey = Deno.env.get('SUPABASE_ANON_KEY') ?? ''

    const supabase = createClient(supabaseUrl, supabaseAnonKey, {
        global: { headers: { Authorization: authHeader } }
    })

    // 1. Validar identidad del usuario
    const { data: { user }, error: authError } = await supabase.auth.getUser()
    if (authError || !user) {
        throw new Response(JSON.stringify({ error: 'Token inválido' }), { status: 401 })
    }

    // 2. Validar que el perfil esté completo en la base de datos
    const { data: perfil, error: perfilError } = await supabase
    .from('user_profile')
    .select('*')
    .eq('user_profile_id', user.id)
    .single()

    // 1. CAPTURA SI ES NULL (No existe el registro o la respuesta está vacía)
    if (!perfil) {
        console.log("data perfil:",perfil)
        throw new Response(
            JSON.stringify({ code: "PROFILE_NOT_FOUND", message: "El perfil no existe" }),
            { status: 404, headers: { "Content-Type": "application/json" } }
        );
    }

    // 2. CAPTURA SI HUBO UN ERROR DE CONEXIÓN O DE SINTAXIS
    if (perfilError) {
        console.log("data perfilError:",perfilError)
        throw new Response(
            JSON.stringify({ code: "DB_ERROR", message: perfilError.message }),
            { status: 401, headers: { "Content-Type": "application/json" } }
        );
    }

    // 3. CAPTURA SI EL PERFIL EXISTE PERO ESTÁ INCOMPLETO
    // (Aquí TypeScript ya sabe que 'perfil' NO es null gracias al primer IF)
    if (perfil.is_full_profile !== true) {
        console.log("data is_full_profile:",is_full_profile)
        throw new Response(
            JSON.stringify({ code: "PROFILE_INCOMPLETE", message: "Perfil incompleto" }),
            { status: 404, headers: { "Content-Type": "application/json" } }
        );
    }

    // Si todo está bien, devolvemos las herramientas necesarias para la función
    return { userId: user.id, userEmail:user.email, userType:perfil.user_type, supabase }

}