package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.CourseEnrollResponse
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity
import com.erns.alertauni.data.remote.StudentDataSource
import javax.inject.Inject

class StudentRepositoryImpl@Inject constructor(
    private val studentDataSource: StudentDataSource
): StudentRepository {
    override suspend fun getStudentEnrollment(): Result<List<StudentEnrollment>> {
        return try {
            val success = studentDataSource.callStudentEnrollmentEndpoint()
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun findCourse(classCode: ClassCodeRequest): Result<StudentEnrollment> {
        return try {
            val success = studentDataSource.callFindCourseEndpoint(classCode)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun courseEnroll(courseEnrollRequest: CourseEnrollRequest): Result<CourseEnrollResponse> {
        return try {
            val success = studentDataSource.callCourseEnrollEndpoint(courseEnrollRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun courseStudent(courseCatalogRequest: CourseCatalogRequest): Result<List<StudentEntity>> {
        return try {
            val success = studentDataSource.callCourseStudentEndpoint(courseCatalogRequest)
            Result.success(success.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}