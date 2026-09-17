package com.fittracker.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fittracker.app.data.local.dao.*
import com.fittracker.app.data.local.entities.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.time.LocalDate

@Database(
    entities = [
        Exercise::class,
        Routine::class,
        RoutineExercise::class,
        WorkoutLog::class,
        FoodLog::class,
        UserTargets::class,
        DailyActivitySummary::class,
        FoodCache::class,
        WeightLog::class,
        UserProfile::class,
        BodyCompositionLog::class,
        WaterLog::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun userTargetsDao(): UserTargetsDao
    abstract fun dailyActivitySummaryDao(): DailyActivitySummaryDao
    abstract fun foodCacheDao(): FoodCacheDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyCompositionDao(): BodyCompositionDao
    abstract fun waterLogDao(): WaterLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fittracker_database"
                )
                    .addCallback(DatabaseCallback(context, scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    populateInitialData(context, INSTANCE ?: return@launch)
                }
            }
        }

        suspend fun populateInitialData(context: Context, db: AppDatabase) {
            val exerciseDao = db.exerciseDao()
            val userTargetsDao = db.userTargetsDao()
            val profileDao = db.userProfileDao()

            // 1. Perfil de usuario inicial limpio
            if (profileDao.getUserProfileDirect() == null) {
                val defaultProfile = UserProfile(
                    id = 1,
                    gender = "MALE",
                    age = 25,
                    heightCm = 175.0,
                    weightKg = 70.0,
                    activityLevel = "MODERATE",
                    goal = "MAINTENANCE"
                )
                profileDao.upsertProfile(defaultProfile)
                userTargetsDao.upsertUserTargets(defaultProfile.calculateRecommendedTargets())
            }

            // 2. Sembrar ejercicios desde assets/exercises.json (catálogo completo OpenGym de 1.352 ejercicios)
            if (exerciseDao.getExercisesCount() < 1000) {
                try {
                    val assetManager = context.assets
                    assetManager.open("exercises.json").use { inputStream ->
                        val reader = InputStreamReader(inputStream)
                        val exerciseListType = object : TypeToken<List<Exercise>>() {}.type
                        val exercises: List<Exercise> = Gson().fromJson(reader, exerciseListType)
                        exerciseDao.insertExercises(exercises)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        suspend fun clearAllUserData(db: AppDatabase) {
            db.weightLogDao().deleteAll()
            db.bodyCompositionDao().deleteAll()
            db.foodLogDao().deleteAll()
            db.workoutLogDao().deleteAll()
            db.routineDao().deleteAllRoutineExercises()
            db.routineDao().deleteAllRoutines()
            db.dailyActivitySummaryDao().deleteAll()
        }
    }
}
