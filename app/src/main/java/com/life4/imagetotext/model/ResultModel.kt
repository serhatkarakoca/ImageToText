package com.life4.imagetotext.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "result")
data class ResultModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String?,
    val date: String
)
