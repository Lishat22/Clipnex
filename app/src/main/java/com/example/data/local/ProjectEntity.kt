package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val clipCount: Int,
    val aspectRatio: String,
    val captionLanguage: String,
    val status: String,
    val progressPercent: Int,
    val statusBengali: String,
    val createdAt: Long,
    val isDemo: Boolean,
    val errorMessage: String? = null
)

@Entity(
    tableName = "clips",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ClipEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val durationSeconds: Int,
    val viralityScore: Int,
    val hookScore: Int,
    val flowScore: Int,
    val valueScore: Int,
    val trendScore: Int,
    val downloadUrl: String,
    val videoUrl: String,
    val transcript: String,
    val postCaption: String,
    val hashtagsString: String,
    val isFavorite: Boolean = false
)

data class ProjectWithClips(
    @Embedded val project: ProjectEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val clips: List<ClipEntity>
)
