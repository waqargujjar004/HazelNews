package com.example.hazelnews.di

import android.content.Context
import androidx.room.Room
import com.example.hazelnews.data.local.db.ArticleDAO
import com.example.hazelnews.data.local.db.ArticleDatabase
import com.example.hazelnews.data.local.datasources.LocalDataSource
import com.example.hazelnews.data.local.datasources.LocalDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ArticleDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ArticleDatabase::class.java,
            "article_db.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: ArticleDatabase): ArticleDAO {
        return database.getArticleDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalDataSourceModule {

    @Binds
    abstract fun bindLocalDataSource(
        localDataSourceImpl: LocalDataSourceImpl
    ): LocalDataSource
}
