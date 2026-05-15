package com.grameenlight.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grameenlight.data.local.entity.PoleEntity
import com.grameenlight.domain.model.PoleStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPole(pole: PoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPoles(poles: List<PoleEntity>)

    @Query("SELECT * FROM poles")
    suspend fun getAllPoles(): List<PoleEntity>

    @Query("SELECT * FROM poles WHERE poleId = :poleId")
    suspend fun getPoleById(poleId: String): PoleEntity?

    @Query("SELECT * FROM poles WHERE currentStatus = :status")
    suspend fun getPolesByStatus(status: PoleStatus): List<PoleEntity>

    @Query("SELECT * FROM poles")
    fun getAllPolesAsFlow(): Flow<List<PoleEntity>>

    @Query("DELETE FROM poles")
    suspend fun clearAllPoles()
}
