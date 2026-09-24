// Setup type definitions for built-in Supabase Runtime APIs
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { authProfile } from '@shared/auth2.ts'

serve(async (req: Request) => {
  try {

    const { user, user_type, supabase } = await authProfile(req)

    let userType: string;
    if (user_type === 1) {
        userType = "PROFESSOR";
    } else if (user_type === 2) {
        userType = "STUDENT";
    }
    else {
        userType = "";
    }

    const data = {
        user_email: user.email,
        user_type: userType
    }

    return new Response(
        JSON.stringify({data: data}), 
        { status: 200, headers: { "Content-Type": "application/json" } }
    )

  } catch (error) {
    console.log(error)
    if (error instanceof Response) {
      return error;
    }
    return new Response(JSON.stringify({ error: error.message }), { status: 401 })
  }
})