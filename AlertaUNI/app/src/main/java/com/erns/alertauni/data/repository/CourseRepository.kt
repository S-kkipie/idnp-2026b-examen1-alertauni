package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.CourseCatalogEntity

interface CourseRepository {
    suspend fun getCourseCatalogList(): Result<List<CourseCatalogEntity>>
}