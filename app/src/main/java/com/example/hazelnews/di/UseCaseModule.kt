package com.example.hazelnews.di

import com.example.hazelnews.data.repository.NewsRepository
import com.example.hazelnews.domain.usecases.DeleteArticleUseCase
import com.example.hazelnews.domain.usecases.usecasesImpl.DeleteArticleUseCaseImpl
import com.example.hazelnews.domain.usecases.GetFavoriteNewsUseCase
import com.example.hazelnews.domain.usecases.usecasesImpl.GetFavoriteNewsUseCaseImpl
import com.example.hazelnews.domain.usecases.usecasesImpl.GetHeadlinesUseCaseImpl
import com.example.hazelnews.domain.usecases.IsArticleExistsUseCase
import com.example.hazelnews.domain.usecases.usecasesImpl.IsArticleExistsUseCaseImpl
import com.example.hazelnews.domain.usecases.SearchNewsUseCase
import com.example.hazelnews.domain.usecases.usecasesImpl.SearchNewsUseCaseImpl
import com.example.hazelnews.domain.usecases.UpsertArticleUseCase
import com.example.hazelnews.domain.usecases.usecasesImpl.UpsertArticleUseCaseImpl
import com.example.hazelnews.domain.usecases.usecasesInterface.GetHeadlinesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
   // @Singleton
    fun provideGetHeadlinesUseCase(newsRepository: NewsRepository): GetHeadlinesUseCase {
        return GetHeadlinesUseCaseImpl(newsRepository)
    }

   @Provides
    fun provideSearchNewsUseCase(searchNewsUseCaseImpl: SearchNewsUseCaseImpl): SearchNewsUseCase {
        return searchNewsUseCaseImpl
    }

    @Provides
    fun provideDeleteArticleUseCase(deleteArticleUseCaseImpl: DeleteArticleUseCaseImpl): DeleteArticleUseCase {
        return deleteArticleUseCaseImpl
    }
    @Provides
    fun provideGetFavoriteNewsUseCase(getFavoriteNewsUseCaseImpl: GetFavoriteNewsUseCaseImpl): GetFavoriteNewsUseCase {
        return getFavoriteNewsUseCaseImpl
    }

    @Provides
    fun provideIsArticleExistsUseCase(isArticleExistsUseCaseImpl: IsArticleExistsUseCaseImpl): IsArticleExistsUseCase {
        return isArticleExistsUseCaseImpl
    }

    @Provides
    fun provideUpsertArticleUseCase(upsertArticleUseCaseImpl: UpsertArticleUseCaseImpl): UpsertArticleUseCase {
        return upsertArticleUseCaseImpl
    }

}