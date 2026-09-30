package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.ClassCodeActionRequest
import com.erns.alertauni.data.model.ClassCodeInfo
import com.erns.alertauni.data.model.CourseCatalogEntity

interface CourseRepository {
    suspend fun getCourseCatalogList(): Result<List<CourseCatalogEntity>>
    suspend fun classCode(request: ClassCodeActionRequest): Result<ClassCodeInfo>
}
