package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.UserProfileDao
import com.fittracker.app.data.local.dao.UserTargetsDao
import com.fittracker.app.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val userTargetsDao: UserTargetsDao
) {
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfileFlow()

    suspend fun getProfileDirect(): UserProfile? {
        return userProfileDao.getUserProfileDirect()
    }

    suspend fun updateProfile(profile: UserProfile, applyRecommendedTargets: Boolean = true) {
        userProfileDao.upsertProfile(profile)
        if (applyRecommendedTargets) {
            val recommendedTargets = profile.calculateRecommendedTargets()
            userTargetsDao.upsertUserTargets(recommendedTargets)
        }
    }

    suspend fun syncProfileWeight(newWeightKg: Double) {
        val current = userProfileDao.getUserProfileDirect() ?: UserProfile()
        val updated = current.copy(weightKg = newWeightKg)
        userProfileDao.upsertProfile(updated)
    }
}
