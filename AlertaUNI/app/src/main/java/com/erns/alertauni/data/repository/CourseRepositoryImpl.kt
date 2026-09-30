package com.erns.alertauni.data.repository

import com.erns.alertauni.common.AppEvent
import com.erns.alertauni.common.AppEventManager
import com.erns.alertauni.data.model.BackendException
import com.erns.alertauni.data.model.ClassCodeActionRequest
import com.erns.alertauni.data.model.ClassCodeInfo
import com.erns.alertauni.data.model.CourseCatalogEntity
import com.erns.alertauni.data.model.EnrollmentRequest
import com.erns.alertauni.data.model.EnrollmentRequestAction
import com.erns.alertauni.data.remote.CourseDataSource
import javax.inject.Inject

class CourseRepositoryImpl@Inject constructor(
    private val courseDataSource: CourseDataSource
): CourseRepository {
    override suspend fun getCourseCatalogList(): Result<List<CourseCatalogEntity>> {
        return try {
            val success = courseDataSource.callGetCoursesEndpoint()
            if (success.data.isNotEmpty()) {
                Result.success(success.data)
            } else {
                Result.failure(Exception("No posts found"))
            }
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

    override suspend fun classCode(request: ClassCodeActionRequest): Result<ClassCodeInfo> {
        return try {
            Result.success(courseDataSource.callClassCodeEndpoint(request).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEnrollmentRequests(courseCatalogId: String): Result<List<EnrollmentRequest>> {
        return try {
            val response = courseDataSource.callEnrollmentRequestsEndpoint(
                EnrollmentRequestAction(courseCatalogId, EnrollmentRequestAction.LIST)
            )
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reviewEnrollmentRequest(
        courseCatalogId: String,
        requestId: Long,
        approve: Boolean
    ): Result<Unit> {
        return try {
            courseDataSource.callReviewEnrollmentRequestEndpoint(
                EnrollmentRequestAction(
                    courseCatalogId,
                    if (approve) EnrollmentRequestAction.APPROVE else EnrollmentRequestAction.REJECT,
                    requestId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
