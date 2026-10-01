package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.ClassCodeActionRequest
import com.erns.alertauni.data.model.ClassCodeInfo
import com.erns.alertauni.data.model.CourseCatalogEntity
import com.erns.alertauni.data.model.EnrollmentRequest

interface CourseRepository {
    suspend fun getCourseCatalogList(): Result<List<CourseCatalogEntity>>
    suspend fun classCode(request: ClassCodeActionRequest): Result<ClassCodeInfo>
    suspend fun getEnrollmentRequests(courseCatalogId: String): Result<List<EnrollmentRequest>>
    suspend fun reviewEnrollmentRequest(
        courseCatalogId: String,
        requestId: Long,
        approve: Boolean
    ): Result<Unit>
}
