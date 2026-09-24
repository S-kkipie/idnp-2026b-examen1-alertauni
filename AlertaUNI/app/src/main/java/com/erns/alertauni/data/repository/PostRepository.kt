package com.erns.alertauni.data.repository

import com.erns.alertauni.data.model.PostEntity
import com.erns.alertauni.data.model.CatalogCourseEntity
import com.erns.alertauni.data.model.PostAddResponse
import com.erns.alertauni.data.model.PostRequest

interface PostRepository {
    suspend fun sendPost(postRequest: PostRequest): Result<PostAddResponse>
    suspend fun getPosts(): Result<List<PostEntity>>
    suspend fun getCourseCatalog(): Result<List<CatalogCourseEntity>>
}