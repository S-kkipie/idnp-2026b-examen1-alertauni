package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.ClassCodeRequest
import com.erns.alertauni.data.model.CourseCatalogRequest
import com.erns.alertauni.data.model.CourseEnrollRequest
import com.erns.alertauni.data.model.CourseEnrollResponse
import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.StudentEnrollment
import com.erns.alertauni.data.model.StudentEntity

interface StudentRepository {
    suspend fun getStudentEnrollment():Result<List<StudentEnrollment>>
    suspend fun findCourse(classCode: ClassCodeRequest):Result<StudentEnrollment>
    suspend fun courseEnroll(courseEnrollRequest: CourseEnrollRequest):Result<CourseEnrollResponse>
    suspend fun courseStudent(courseCatalogRequest: CourseCatalogRequest):Result<List<StudentEntity>>
}