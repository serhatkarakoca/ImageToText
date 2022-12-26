package com.life4.imagetotext.di

import android.content.Context
import androidx.room.Room
import com.life4.imagetotext.room.ResultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ResultDatabase::class.java, "results").build()

    @Provides
    fun provideRoomDao(resultDatabase: ResultDatabase) = resultDatabase.resultDao()
}