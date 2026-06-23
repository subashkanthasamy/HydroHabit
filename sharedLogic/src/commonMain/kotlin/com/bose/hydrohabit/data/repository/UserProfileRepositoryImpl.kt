package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.UserProfile
import com.bose.hydrohabit.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserProfileRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UserProfileRepository {

    private val queries = db.userProfileQueries

    override fun observeProfile(): Flow<UserProfile?> =
        queries.selectProfile().asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = withContext(dispatcher) {
        queries.selectProfile().executeAsOneOrNull()?.toDomain()
    }

    override suspend fun saveProfile(profile: UserProfile): AppResult<Unit> = dbWrite(dispatcher) {
        queries.upsertProfile(
            id = profile.id,
            weightKg = profile.weightKg,
            age = profile.age.toLong(),
            gender = profile.gender?.name,
            activityLevel = profile.activityLevel.name,
            wakeTime = profile.wakeTime.toString(),
            sleepTime = profile.sleepTime.toString(),
            unitSystem = profile.unitSystem.name,
            createdAt = profile.createdAt.toEpochMilliseconds(),
            updatedAt = profile.updatedAt.toEpochMilliseconds(),
        )
    }
}
