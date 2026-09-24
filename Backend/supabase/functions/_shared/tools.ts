export async function getCourseCatalogIDs(
    userType: number, 
    userId: string, 
    supabase: SupabaseClient
): Promise<{ course_catalog_id: string }[]>  {

    const PROFESSOR_TYPE:number = 1
    const STUDENT_TYPE:number = 2

    if(userType == PROFESSOR_TYPE){
        const { data, error } = await supabase.from('course_catalog')
        .select('course_catalog_id')
        .eq('professor_id',userId)

        if (error) throw error

        return data
    }
    else if(userType == STUDENT_TYPE){
        const { data, error } = await supabase.from('student_enrollment')
        .select('course_catalog_id')
        .eq('student_id',userId)

        if (error) throw error

        return data
    }

     return null

}

export async function getUserType(userId: string, supabase: SupabaseClient): number {
        const { data, error } = await supabase.from('user_profile')
        .select('user_type')
        .eq('user_profile_id',userId)
        .eq('is_full_profile',true)
        .single()

        if (error) throw error

        return data?.user_type ?? null
}
