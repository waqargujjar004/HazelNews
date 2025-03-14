package com.example.hazelnews.di

import com.example.hazelnews.data.remote.api.NewsApI
import com.example.hazelnews.data.remote.datasource.RemoteDataSource
import com.example.hazelnews.data.remote.datasource.RemoteDataSourceImpl
import com.example.hazelnews.util.Constants.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsApi(retrofit: Retrofit): NewsApI {
        return retrofit.create(NewsApI::class.java)
    }

    @Provides
    @Singleton
    fun provideRemoteDataSource(newsApi: NewsApI): RemoteDataSource {
        return RemoteDataSourceImpl(newsApi)
    }
}
