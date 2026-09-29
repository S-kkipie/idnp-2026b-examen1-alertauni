package com.erns.alertauni.data.repository

import com.erns.alertauni.common.AppEvent
import com.erns.alertauni.common.AppEventManager
import com.erns.alertauni.data.model.BackendError
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostRequest
import com.erns.alertauni.data.remote.PostDataSource
import io.github.jan.supabase.exceptions.HttpRequestException
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import javax.inject.Inject
import kotlin.collections.isNotEmpty

class PostRepositoryImpl @Inject constructor(
    private val postDataSource: PostDataSource
) : PostRepository {

    override suspend fun sendPost(postRequest: PostRequest): Result<PostAddResponse> {
        return try {
            val success = postDataSource.callInsertEndpoint(postRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getPosts(): Result<List<PostEntity>> {
        return try {
            val success = postDataSource.callPostsEndpoint()
            // Una lista vacía es un resultado válido (curso sin anuncios), no un error
            Result.success(success.data)
        } catch (e: BackendException) {
            if (e.errorData.code == "PROFILE_NOT_FOUND") {
                // Disparamos el evento global de navegación asíncronamente
                AppEventManager.emit(AppEvent.NavigateToCompleteProfile)
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun getCourseCatalog(): Result<List<CatalogCourseEntity>> {
        return try {
            val success = postDataSource.callCourseCatalogEndpoint()
            if (success.data.isNotEmpty()) {
//                val catalogCourses = success.data.map { course ->
//                    CatalogCourseEntity(
//                        id = course.id,
//                        courseCode = course.courseCode,
//                        courseName = course.courseName
//                    )
//                }
//                Result.success(catalogCourses)
                Result.success(success.data)
            } else {
                Result.failure(Exception("The course catalog is empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}