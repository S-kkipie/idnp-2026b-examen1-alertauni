package com.erns.alertauni.data.di

import com.erns.alertauni.data.repository.AccountRepository
import com.erns.alertauni.data.repository.AccountRepositoryImpl
import com.erns.alertauni.data.repository.AnnounceRepository
import com.erns.alertauni.data.repository.AnnounceRepositoryImpl
import com.erns.alertauni.data.repository.AuthRepository
import com.erns.alertauni.data.repository.AuthRepositoryImpl
import com.erns.alertauni.data.repository.CommentRepository
import com.erns.alertauni.data.repository.CommentRepositoryImpl
import com.erns.alertauni.data.repository.ContactRepository
import com.erns.alertauni.data.repository.ContactRepositoryImpl
import com.erns.alertauni.data.repository.CourseRepository
import com.erns.alertauni.data.repository.CourseRepositoryImpl
import com.erns.alertauni.data.repository.NotificationRepository
import com.erns.alertauni.data.repository.NotificationRepositoryImpl
import com.erns.alertauni.data.repository.PostRepository
import com.erns.alertauni.data.repository.PostRepositoryImpl
import com.erns.alertauni.data.repository.StudentRepository
import com.erns.alertauni.data.repository.StudentRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        commentRepositoryImpl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(
        studentRepositoryImpl: StudentRepositoryImpl
    ): StudentRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindAnnounceRepository(
        announceRepositoryImpl: AnnounceRepositoryImpl
    ): AnnounceRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository
}
