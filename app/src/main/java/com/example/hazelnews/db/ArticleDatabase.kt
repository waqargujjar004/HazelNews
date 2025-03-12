package com.example.hazelnews.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hazelnews.models.Article

@Database(
    entities = [Article::class],
    version = 2
)

@TypeConverters(Converters::class)
abstract class ArticleDatabase: RoomDatabase() {

    abstract fun getArticleDao(): ArticleDAO


    companion object {

        @Volatile
         private var instance: ArticleDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance?: synchronized(Lock){
            instance?: createDatabase(context).also{
                instance = it

            }
        }

        private fun createDatabase(context: Context) =
             Room.databaseBuilder(
                 context.applicationContext,
                 ArticleDatabase::class.java,
                  "article_db.db"

             ).fallbackToDestructiveMigration().build()


    }




}