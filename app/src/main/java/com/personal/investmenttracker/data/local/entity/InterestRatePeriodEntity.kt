package com.personal.investmenttracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "interest_rate_periods",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("accountId"), Index("effectiveFrom")],
)
data class InterestRatePeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val annualRateBasisPoints: Int,
    val effectiveFrom: LocalDate,
    val effectiveTo: LocalDate?,
    val sourceNote: String?,
)
