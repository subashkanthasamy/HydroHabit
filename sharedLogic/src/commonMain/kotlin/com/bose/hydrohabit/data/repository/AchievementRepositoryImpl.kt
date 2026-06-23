package com.bose.hydrohabit.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bose.hydrohabit.core.AppResult
import com.bose.hydrohabit.data.mapper.toDomain
import com.bose.hydrohabit.db.HydroHabitDatabase
import com.bose.hydrohabit.domain.model.Achievement
import com.bose.hydrohabit.domain.model.AchievementId
import com.bose.hydrohabit.domain.repository.AchievementRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class AchievementRepositoryImpl(
    db: HydroHabitDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AchievementRepository {

    private val queries = db.achievementQueries

    override fun observeAchievements(): Flow<List<Achievement>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getAchievements(): List<Achievement> = withContext(dispatcher) {
        queries.selectAll().executeAsList().map { it.toDomain() }
    }

    override suspend fun upsertAll(achievements: List<Achievement>): AppResult<Unit> = dbWrite(dispatcher) {
        queries.transaction {
            achievements.forEach { a ->
                queries.upsertAchievement(
                    id = a.id.name,
                    title = a.title,
                    description = a.description,
                    type = a.type.name,
                    threshold = a.threshold,
                    unlockedAt = a.unlockedAt?.toEpochMilliseconds(),
                    progress = a.progress.toDouble(),
                )
            }
        }
    }

    override suspend fun unlock(id: AchievementId, at: Instant): AppResult<Unit> = dbWrite(dispatcher) {
        queries.unlock(unlockedAt = at.toEpochMilliseconds(), id = id.name)
    }
}
