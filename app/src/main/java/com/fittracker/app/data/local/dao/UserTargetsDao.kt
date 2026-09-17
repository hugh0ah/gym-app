package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.UserTargets
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTargetsDao {
    @Query("SELECT * FROM user_targets WHERE id = 1 LIMIT 1")
    fun getUserTargetsFlow(): Flow<UserTargets?>

    @Query("SELECT * FROM user_targets WHERE id = 1 LIMIT 1")
    suspend fun getUserTargetsDirect(): UserTargets?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserTargets(targets: UserTargets)
}
