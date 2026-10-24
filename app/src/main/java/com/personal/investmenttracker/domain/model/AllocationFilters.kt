package com.personal.investmenttracker.domain.model

val traditionalAllocationTypes: Set<InvestmentType> = setOf(
    InvestmentType.PPF,
    InvestmentType.SSA,
    InvestmentType.PF,
    InvestmentType.FD,
    InvestmentType.RD,
)

val providentPlusStocksAllocationTypes: Set<InvestmentType> =
    traditionalAllocationTypes + InvestmentType.STOCKS

/** Type slices for the traditional-scope “by type” pie (PPF, SSA, PF, FD, RD). */
val traditionalTypePieOrder: List<InvestmentType> = listOf(
    InvestmentType.PPF,
    InvestmentType.SSA,
    InvestmentType.PF,
    InvestmentType.FD,
    InvestmentType.RD,
)

val providentPlusStocksTypePieOrder: List<InvestmentType> =
    traditionalTypePieOrder + InvestmentType.STOCKS

val allHoldingsTypePieOrder: List<InvestmentType> = listOf(
    InvestmentType.PPF,
    InvestmentType.SSA,
    InvestmentType.PF,
    InvestmentType.FD,
    InvestmentType.RD,
    InvestmentType.STOCKS,
    InvestmentType.METALS,
    InvestmentType.REAL_ESTATE,
    InvestmentType.NPS,
)

enum class AllocationPieGrouping {
    BY_ACCOUNT,
    BY_TYPE,
    BY_ASSET_CLASS,
}

enum class PortfolioAssetClass(val label: String, val types: Set<InvestmentType>) {
    SAFE_DEBT("Safe debt", traditionalAllocationTypes),
    STOCKS("Stocks", setOf(InvestmentType.STOCKS)),
    REAL_ESTATE("Real estate", setOf(InvestmentType.REAL_ESTATE)),
    RETIREMENT("Retirement", setOf(InvestmentType.NPS)),
    METALS("Metals", setOf(InvestmentType.METALS)),
}

val portfolioAssetClassPieOrder: List<PortfolioAssetClass> = listOf(
    PortfolioAssetClass.SAFE_DEBT,
    PortfolioAssetClass.STOCKS,
    PortfolioAssetClass.REAL_ESTATE,
    PortfolioAssetClass.RETIREMENT,
    PortfolioAssetClass.METALS,
)

enum class DashboardPortfolioScope {
    TRADITIONAL,
    PROVIDENT_PLUS_STOCKS,
    ALL_HOLDINGS,
}

data class DashboardAllocationPage(
    val subtitle: String,
    val includeTypes: Set<InvestmentType>?,
    val grouping: AllocationPieGrouping,
    val typeOrder: List<InvestmentType>? = null,
    val assetClassOrder: List<PortfolioAssetClass>? = null,
    val portfolioScope: DashboardPortfolioScope,
)

val dashboardAllocationPages: List<DashboardAllocationPage> = listOf(
    DashboardAllocationPage(
        subtitle = "PPF · SSA · PF · FD · RD",
        includeTypes = traditionalAllocationTypes,
        grouping = AllocationPieGrouping.BY_ACCOUNT,
        portfolioScope = DashboardPortfolioScope.TRADITIONAL,
    ),
    DashboardAllocationPage(
        subtitle = "PPF · SSA · PF · FD · RD",
        includeTypes = traditionalAllocationTypes,
        grouping = AllocationPieGrouping.BY_TYPE,
        typeOrder = traditionalTypePieOrder,
        portfolioScope = DashboardPortfolioScope.TRADITIONAL,
    ),
    DashboardAllocationPage(
        subtitle = "PPF · SSA · PF · FD · RD + stocks",
        includeTypes = providentPlusStocksAllocationTypes,
        grouping = AllocationPieGrouping.BY_ACCOUNT,
        portfolioScope = DashboardPortfolioScope.PROVIDENT_PLUS_STOCKS,
    ),
    DashboardAllocationPage(
        subtitle = "PPF · SSA · PF · FD · RD + stocks",
        includeTypes = providentPlusStocksAllocationTypes,
        grouping = AllocationPieGrouping.BY_TYPE,
        typeOrder = providentPlusStocksTypePieOrder,
        portfolioScope = DashboardPortfolioScope.PROVIDENT_PLUS_STOCKS,
    ),
    DashboardAllocationPage(
        subtitle = "All holdings",
        includeTypes = null,
        grouping = AllocationPieGrouping.BY_ACCOUNT,
        portfolioScope = DashboardPortfolioScope.ALL_HOLDINGS,
    ),
    DashboardAllocationPage(
        subtitle = "All holdings",
        includeTypes = null,
        grouping = AllocationPieGrouping.BY_TYPE,
        typeOrder = allHoldingsTypePieOrder,
        portfolioScope = DashboardPortfolioScope.ALL_HOLDINGS,
    ),
    DashboardAllocationPage(
        subtitle = "Safe debt · stocks · real estate · retirement · metals",
        includeTypes = null,
        grouping = AllocationPieGrouping.BY_ASSET_CLASS,
        assetClassOrder = portfolioAssetClassPieOrder,
        portfolioScope = DashboardPortfolioScope.ALL_HOLDINGS,
    ),
)

val DASHBOARD_ALLOCATION_PAGE_COUNT: Int = dashboardAllocationPages.size

fun dashboardAllocationPage(index: Int): DashboardAllocationPage =
    dashboardAllocationPages[index.coerceIn(0, dashboardAllocationPages.lastIndex)]

fun dashboardAllocationSubtitle(page: Int): String = dashboardAllocationPage(page).subtitle

fun allocationTypesForDashboardPage(page: Int): Set<InvestmentType>? =
    dashboardAllocationPage(page).includeTypes
