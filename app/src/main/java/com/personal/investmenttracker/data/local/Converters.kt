package com.personal.investmenttracker.data.local

import androidx.room.TypeConverter
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.model.TransactionType
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun investmentTypeToString(value: InvestmentType): String = value.name

    @TypeConverter
    fun stringToInvestmentType(value: String): InvestmentType = InvestmentType.valueOf(value)

    @TypeConverter
    fun transactionTypeToString(value: TransactionType): String = value.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)
}
