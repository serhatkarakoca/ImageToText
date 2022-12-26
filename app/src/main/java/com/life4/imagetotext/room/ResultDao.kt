package com.life4.imagetotext.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.life4.imagetotext.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultModel): Long

    @Query("Select * from result")
    fun getAllResults(): Flow<List<ResultModel>>

    @Query("Delete from result where id == :pId")
    suspend fun deleteResult(pId: Long)

    @Query("Update result SET content = :content where id == :pId")
    suspend fun updateResult(pId: Long, content: String?)

    @Query("Delete from result")
    suspend fun deleteAllRecords()
}