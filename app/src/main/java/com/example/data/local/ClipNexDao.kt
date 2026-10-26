package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipNexDao {
    @Transaction
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjectsWithClips(): Flow<List<ProjectWithClips>>

    @Transaction
    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun getProjectWithClipsById(projectId: String): Flow<ProjectWithClips?>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET status = :status, progressPercent = :progress, statusBengali = :bengaliStatus, errorMessage = :error WHERE id = :projectId")
    suspend fun updateProjectStatus(projectId: String, status: String, progress: Int, bengaliStatus: String, error: String? = null)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClips(clips: List<ClipEntity>)

    @Update
    suspend fun updateClip(clip: ClipEntity)

    @Query("UPDATE clips SET title = :title, transcript = :transcript, postCaption = :postCaption WHERE id = :clipId")
    suspend fun updateClipContent(clipId: String, title: String, transcript: String, postCaption: String)

    @Query("UPDATE clips SET isFavorite = :isFavorite WHERE id = :clipId")
    suspend fun toggleClipFavorite(clipId: String, isFavorite: Boolean)

    @Query("DELETE FROM clips WHERE projectId = :projectId")
    suspend fun deleteClipsByProjectId(projectId: String)
}
