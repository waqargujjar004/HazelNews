package com.example.hazelnews.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "articles"
)
@Parcelize
data class Article(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source,
    val title: String?,
    val url: String?,
    val urlToImage: String?

) : Parcelable {

    fun isEqual(other: Article): Boolean {

        return author == other.author &&
                content == other.content &&
                description == other.description &&
                publishedAt == other.publishedAt &&
                source == other.source &&
                title == other.title &&
                url == other.url &&
                urlToImage == other.urlToImage
    }

}