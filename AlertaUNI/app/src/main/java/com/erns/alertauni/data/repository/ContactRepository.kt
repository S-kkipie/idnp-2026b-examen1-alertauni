package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.StudentEntity

interface ContactRepository {
    suspend fun getStudentsByCourse(courseCatalogRequest: CourseCatalogRequest):Result<List<StudentEntity>>
}