package com.personal.investmenttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.personal.investmenttracker.domain.model.InvestmentType
import java.time.LocalDate

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: InvestmentType,
    val openingBalanceMinor: Long,
    val openingBalanceDate: LocalDate,
    val openingBalanceIncludesCreditedInterest: Boolean,
    val createdAtEpochMillis: Long,
    val notes: String?,
    val fixedAnnualRateBasisPoints: Int? = null,
    val termEndDate: LocalDate? = null,
    val rdMonthlyAmountMinor: Long? = null,
    val termStartYear: Int? = null,
    val termStartMonth: Int? = null,
    val termEndYear: Int? = null,
    val termEndMonth: Int? = null,
    val shareQuantity: Double? = null,
    val currentSharePriceMinor: Long? = null,
    val currentValueMinor: Long? = null,
)
