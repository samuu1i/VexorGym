package com.example.vexorgym.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Instant

@Serializable
data class WorkoutSession(
    val id: String,
    @SerialName("exercise_id") val exerciseId: String = "",
    @SerialName("date_created") val dateCreated: String? = null,
    val sets: List<WorkoutSet> = emptyList(),
    @Transient val createdAtMillis: Long = parsePostgresTimestamp(dateCreated),
    @Transient val isPending: Boolean = false,
)

internal fun parsePostgresTimestamp(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    return runCatching {
        Instant.parse(value).toEpochMilliseconds()
    }.getOrElse {
        runCatching {
            Instant.parse(value.replace(" ", "T")).toEpochMilliseconds()
        }.getOrDefault(0L)
    }
}
