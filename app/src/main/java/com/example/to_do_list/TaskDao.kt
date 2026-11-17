package com.example.to_do_list

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun observeTasks(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task): Int

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    suspend fun setCompleted(taskId: Int, completed: Boolean): Int

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Int): Int
}
