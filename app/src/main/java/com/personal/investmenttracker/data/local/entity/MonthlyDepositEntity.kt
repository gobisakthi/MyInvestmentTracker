package com.personal.investmenttracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_deposits",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["accountId", "year", "month"], unique = true),
    ],
)
data class MonthlyDepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val year: Int,
    val month: Int,
    val amountMinor: Long,
    val updatedAtEpochMillis: Long,
)
