package com.fittracker.app

import android.app.Application
import com.fittracker.app.data.local.ApiKeyManager
import com.fittracker.app.data.local.AppDatabase
import com.fittracker.app.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitTrackerApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val apiKeyManager by lazy { ApiKeyManager.getInstance(this) }

    val exerciseRepository by lazy {
        ExerciseRepository(database.exerciseDao())
    }

    val routineRepository by lazy {
        RoutineRepository(database.routineDao(), database.workoutLogDao())
    }

    val foodRepository by lazy {
        FoodRepository(
            database.foodLogDao(),
            database.foodCacheDao(),
            database.userTargetsDao()
        )
    }

    val activityRepository by lazy {
        ActivityRepository(database.dailyActivitySummaryDao())
    }

    val weightRepository by lazy {
        WeightRepository(database.weightLogDao())
    }

    val waterRepository by lazy {
        WaterRepository(database.waterLogDao())
    }

    val userProfileRepository by lazy {
        UserProfileRepository(
            database.userProfileDao(),
            database.userTargetsDao()
        )
    }

    val bodyCompositionRepository by lazy {
        BodyCompositionRepository(
            database.bodyCompositionDao(),
            database.weightLogDao(),
            userProfileRepository
        )
    }

    val aiAssistantRepository by lazy {
        AiAssistantRepository(
            routineRepository = routineRepository,
            activityRepository = activityRepository,
            exerciseRepository = exerciseRepository,
            bodyCompositionRepository = bodyCompositionRepository,
            foodRepository = foodRepository,
            weightRepository = weightRepository,
            waterRepository = waterRepository,
            apiKeyManager = apiKeyManager
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            AppDatabase.populateInitialData(this@FitTrackerApp, database)
        }
    }
}
