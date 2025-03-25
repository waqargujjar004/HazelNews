package com.example.hazelnews.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hazelnews.domain.models.Article



@Database(
    entities = [Article::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDAO

}








