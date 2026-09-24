package com.erns.alertauni.data.remote

import com.erns.alertauni.common.safeInvoke
import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.CourseEnrollResponse
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.model.SupabaseListResponse
import com.erns.alertauni.data.model.SupabaseSimpleResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class StudentDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun callStudentEnrollmentEndpoint(): SupabaseListResponse<StudentEnrollment> {
        val response = supabaseClient.functions.safeInvoke(ApiConstants.GET_STUDENT_ENROLLMENT)
        if (response.status.value in 200..299) {
            return response.body<SupabaseListResponse<StudentEnrollment>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

    suspend fun callFindCourseEndpoint(classCode: ClassCodeRequest): SupabaseSimpleResponse<StudentEnrollment> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.FIND_COURSE_CATALOG, classCode)
        if (response.status.value in 200..299) {
            return response.body<SupabaseSimpleResponse<StudentEnrollment>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

    suspend fun callCourseEnrollEndpoint(courseEnrollRequest: CourseEnrollRequest): SupabaseSimpleResponse<CourseEnrollResponse> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.COURSE_ENROLL, courseEnrollRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseSimpleResponse<CourseEnrollResponse>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

    suspend fun callCourseStudentEndpoint(courseCatalogRequest: CourseCatalogRequest): SupabaseListResponse<StudentEntity> {
        val response =
            supabaseClient.functions.safeInvoke(ApiConstants.COURSE_STUDENT, courseCatalogRequest)
        if (response.status.value in 200..299) {
            return response.body<SupabaseListResponse<StudentEntity>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }

}