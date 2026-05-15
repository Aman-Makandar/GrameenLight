package com.grameenlight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.grameenlight.data.local.entity.ComplaintEntity
import com.grameenlight.data.local.entity.PoleEntity
import com.grameenlight.data.local.entity.UserEntity

@Database(
    entities = [
        PoleEntity::class,
        ComplaintEntity::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poleDao(): PoleDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun userDao(): UserDao
    
    companion object {
        const val DATABASE_NAME = "grameen_light_db"
    }
}
