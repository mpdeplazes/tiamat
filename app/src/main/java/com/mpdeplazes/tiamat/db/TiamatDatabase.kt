package com.mpdeplazes.tiamat.db

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mpdeplazes.tiamat.db.dao.MarkerDao
import com.mpdeplazes.tiamat.db.entity.MarkerEntity
import com.mpdeplazes.tiamat.util.SingletonHolder

@Database(entities = [MarkerEntity::class], version = 1)
abstract class TiamatDatabase : RoomDatabase() {

    abstract fun markerDao(): MarkerDao

    companion object : SingletonHolder<TiamatDatabase, Context>({
        Room.databaseBuilder(
            it.applicationContext,
            TiamatDatabase::class.java,
            "tiamat_db"
        ).build()
    })
}