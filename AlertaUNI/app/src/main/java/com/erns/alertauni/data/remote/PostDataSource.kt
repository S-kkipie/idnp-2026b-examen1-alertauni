package com.erns.alertauni.data.remote

import com.erns.alertauni.common.safeInvoke
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.model.SupabaseListResponse
import com.erns.alertauni.data.model.SupabaseSimpleResponse
import com.erns.alertauni.data.remote.api.ApiConstants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import javax.inject.Inject

class PostDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun callInsertEndpoint(postRequest: PostRequest): SupabaseSimpleResponse<PostAddResponse> {
        //val response = supabaseClient.functions.invoke(ApiConstants.ADD_POST_PUBLIC, postMessage)
        val response = supabaseClient.functions.safeInvoke(ApiConstants.ADD_POST_PUBLIC, postRequest)
        return response.body<SupabaseSimpleResponse<PostAddResponse>>()
        // Return true if status is 201 (Created) or 200 (OK)
        //return response.status.value in 200..299

    }

    suspend fun callPostsEndpoint(): SupabaseListResponse<PostEntity> {
        val response = supabaseClient.functions.safeInvoke(ApiConstants.GET_POSTS)
        return response.body<SupabaseListResponse<PostEntity>>()

    }

    suspend fun callCourseCatalogEndpoint(): SupabaseListResponse<CatalogCourseEntity> {
        val response = supabaseClient.functions.invoke(ApiConstants.GET_COURSE_CATALOG)
        if (response.status.value in 200..299) {
            return response.body<SupabaseListResponse<CatalogCourseEntity>>()
        } else {
            throw Exception("Failed to fetch: ${response.status}")
        }
    }
}