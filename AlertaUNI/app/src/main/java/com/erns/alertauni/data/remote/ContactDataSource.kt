package com.erns.alertauni.data.remote

import com.erns.alertauni.common.safeInvoke
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.model.SupabaseListResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class ContactDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun callStudentByCourseEndpoint(courseCatalogRequest: CourseCatalogRequest): SupabaseListResponse<StudentEntity> {
        val response = supabaseClient.functions.safeInvoke(ApiConstants.COURSE_STUDENT,courseCatalogRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseListResponse<StudentEntity>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }
}