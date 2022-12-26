package com.life4.imagetotext.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.life4.imagetotext.model.ResultModel

@Database(
    entities = [ResultModel::class],
    version = 1,
    exportSchema = false
)
abstract class ResultDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao
}