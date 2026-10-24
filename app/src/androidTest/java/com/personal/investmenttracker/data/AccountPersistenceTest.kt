package com.personal.investmenttracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.investmenttracker.data.local.InvestmentDatabase
import com.personal.investmenttracker.data.repository.AccountRepository
import com.personal.investmenttracker.data.repository.accountCountFor
import com.personal.investmenttracker.data.repository.portfolioRecordedTotalMinor
import com.personal.investmenttracker.data.repository.recordedTotalFor
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.validation.ValidatedAccountForm
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountPersistenceTest {

    private lateinit var context: Context
    private var reopenDatabaseName: String? = null

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        reopenDatabaseName?.let { context.deleteDatabase(it) }
    }

    @Test
    fun insertAndObserve_multipleAccountsAndTotals() = runBlocking {
        val db = inMemoryDatabase()
        val repository = AccountRepository(db.accountDao())

        repository.insertAccount(ppfForm("PPF 1", "100"))
        repository.insertAccount(ppfForm("PPF 2", "200"))
        repository.insertAccount(
            ValidatedAccountForm(
                name = "SSA",
                type = InvestmentType.SSA,
                openingBalanceMinor = 50_000,
                openingBalanceDate = LocalDate.of(2024, 1, 1),
                openingBalanceIncludesCreditedInterest = false,
                notes = null,
            ),
        )

        val summaries = repository.observeAccountSummaries().first()
        assertEquals(3, summaries.size)
        assertEquals(2, summaries.accountCountFor(InvestmentType.PPF))
        assertEquals(30_000L, summaries.recordedTotalFor(InvestmentType.PPF))
        assertEquals(80_000L, summaries.portfolioRecordedTotalMinor())
    }

    @Test
    fun updateAccount_persistsChanges() = runBlocking {
        val db = inMemoryDatabase()
        val repository = AccountRepository(db.accountDao())
        val id = repository.insertAccount(ppfForm("Old name", "1000"))

        repository.updateAccount(
            id,
            ppfForm("New name", "1500.50"),
        )

        val account = repository.getAccount(id)!!
        assertEquals("New name", account.name)
        assertEquals(150_050L, account.openingBalanceMinor)
    }

    @Test
    fun databaseReopen_retainsAccounts() = runBlocking {
        reopenDatabaseName = "test_reopen_${System.currentTimeMillis()}.db"
        context.deleteDatabase(reopenDatabaseName!!)

        val db1 = Room.databaseBuilder(
            context,
            InvestmentDatabase::class.java,
            reopenDatabaseName!!,
        ).build()
        val repository1 = AccountRepository(db1.accountDao())
        repository1.insertAccount(ppfForm("Persisted", "42"))
        db1.close()

        val db2 = Room.databaseBuilder(
            context,
            InvestmentDatabase::class.java,
            reopenDatabaseName!!,
        ).build()
        val repository2 = AccountRepository(db2.accountDao())
        val summaries = repository2.observeAccountSummaries().first()
        db2.close()

        assertEquals(1, summaries.size)
        assertEquals("Persisted", summaries.first().name)
        assertEquals(4_200L, summaries.first().recordedBalanceMinor)
    }

    private fun inMemoryDatabase(): InvestmentDatabase =
        Room.inMemoryDatabaseBuilder(
            context,
            InvestmentDatabase::class.java,
        ).build()

    private fun ppfForm(name: String, rupees: String): ValidatedAccountForm =
        ValidatedAccountForm(
            name = name,
            type = InvestmentType.PPF,
            openingBalanceMinor = (rupees.toBigDecimal().movePointRight(2)).longValueExact(),
            openingBalanceDate = LocalDate.of(2024, 4, 1),
            openingBalanceIncludesCreditedInterest = false,
            notes = null,
        )
}
