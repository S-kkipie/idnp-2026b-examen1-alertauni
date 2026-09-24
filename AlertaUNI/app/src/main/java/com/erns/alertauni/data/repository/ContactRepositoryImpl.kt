package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.remote.ContactDataSource
import javax.inject.Inject

class ContactRepositoryImpl@Inject constructor(
    private val contactDataSource: ContactDataSource
): ContactRepository {
    override suspend fun getStudentsByCourse(courseCatalogRequest: CourseCatalogRequest): Result<List<StudentEntity>> {
        return try {
            val success = contactDataSource.callStudentByCourseEndpoint(courseCatalogRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}