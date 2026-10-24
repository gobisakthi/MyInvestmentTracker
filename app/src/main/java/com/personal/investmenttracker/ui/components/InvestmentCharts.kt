package com.personal.investmenttracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.personal.investmenttracker.data.repository.AccountWithCalculation
import com.personal.investmenttracker.domain.calculation.CalculationStatus
import com.personal.investmenttracker.domain.calculation.MonthlyInterestPoint
import com.personal.investmenttracker.domain.model.InvestmentType
import com.personal.investmenttracker.domain.model.AllocationPieGrouping
import com.personal.investmenttracker.domain.model.PortfolioAssetClass
import com.personal.investmenttracker.domain.model.dashboardAllocationPages
import com.personal.investmenttracker.ui.theme.FdColor
import com.personal.investmenttracker.ui.theme.PfColor
import com.personal.investmenttracker.ui.theme.RdColor
import com.personal.investmenttracker.ui.theme.PieSliceColorInput
import com.personal.investmenttracker.ui.theme.PpfColor
import com.personal.investmenttracker.ui.theme.SsaColor
import com.personal.investmenttracker.ui.theme.TealLight
import com.personal.investmenttracker.ui.theme.TealPrimary
import com.personal.investmenttracker.ui.theme.assignPremiumPieColors
import com.personal.investmenttracker.ui.theme.onPieSliceLabel
import com.personal.investmenttracker.util.formatInrMinor
import java.time.format.TextStyle
import java.util.Locale
import java.util.Locale.US
data class PieSlice(val label: String, val valueMinor: Long, val color: Color)

fun typePieSliceLabel(type: InvestmentType): String = when (type) {
    InvestmentType.STOCKS -> "Stocks"
    InvestmentType.METALS -> "Metals"
    InvestmentType.REAL_ESTATE -> "Real estate"
    InvestmentType.NPS -> "NPS"
    else -> type.name
}

fun colorForAssetClass(assetClass: PortfolioAssetClass): Color = when (assetClass) {
    PortfolioAssetClass.SAFE_DEBT -> TealPrimary
    PortfolioAssetClass.STOCKS -> Color(0xFF7C3AED)
    PortfolioAssetClass.REAL_ESTATE -> Color(0xFF059669)
    PortfolioAssetClass.RETIREMENT -> Color(0xFF2563EB)
    PortfolioAssetClass.METALS -> Color(0xFFD97706)
}

fun pieSlicesByAssetClass(
    accounts: List<AccountWithCalculation>,
    assetClassOrder: List<PortfolioAssetClass>,
): List<PieSlice> =
    assetClassOrder.mapNotNull { assetClass ->
        var totalMinor = 0L
        for (row in accounts) {
            if (row.summary.type !in assetClass.types) continue
            if (row.calculation.status != CalculationStatus.OK) continue
            val valueMinor = row.calculation.estimatedTotalMinor
            if (valueMinor <= 0L) continue
            totalMinor += valueMinor
        }
        if (totalMinor <= 0L) return@mapNotNull null
        PieSlice(
            label = assetClass.label,
            valueMinor = totalMinor,
            color = colorForAssetClass(assetClass),
        )
    }

fun pieSlicesByType(
    accounts: List<AccountWithCalculation>,
    includeTypes: Set<InvestmentType>?,
    typeOrder: List<InvestmentType>,
): List<PieSlice> {
    val scoped = if (includeTypes == null) {
        accounts
    } else {
        accounts.filter { it.summary.type in includeTypes }
    }
    return typeOrder.mapNotNull { type ->
        var totalMinor = 0L
        for (row in scoped) {
            if (row.summary.type != type) continue
            if (row.calculation.status != CalculationStatus.OK) continue
            val valueMinor = row.calculation.estimatedTotalMinor
            if (valueMinor <= 0L) continue
            totalMinor += valueMinor
        }
        if (totalMinor <= 0L) return@mapNotNull null
        PieSlice(
            label = typePieSliceLabel(type),
            valueMinor = totalMinor,
            color = colorForInvestmentType(type),
        )
    }
}

fun pieSlicesForAccounts(
    accounts: List<AccountWithCalculation>,
    includeTypes: Set<InvestmentType>? = null,
): List<PieSlice> {
    val scoped = if (includeTypes == null) {
        accounts
    } else {
        accounts.filter { it.summary.type in includeTypes }
    }
    val eligible = scoped.mapNotNull { row ->
        val valueMinor = when (row.calculation.status) {
            CalculationStatus.OK -> row.calculation.estimatedTotalMinor
            else -> null
        } ?: return@mapNotNull null
        if (valueMinor <= 0L) return@mapNotNull null
        row
    }
    val byType = eligible.groupBy { it.summary.type }
    val sorted = eligible.sortedByDescending { it.calculation.estimatedTotalMinor }
    val colorInputs = sorted.map { row ->
        val peers = byType[row.summary.type].orEmpty()
        val indexAmongType = peers.indexOfFirst { it.summary.id == row.summary.id }.coerceAtLeast(0)
        PieSliceColorInput(type = row.summary.type, indexAmongType = indexAmongType)
    }
    val colors = assignPremiumPieColors(colorInputs)
    return sorted.mapIndexed { index, row ->
        PieSlice(
            label = row.summary.name,
            valueMinor = row.calculation.estimatedTotalMinor,
            color = colors[index],
        )
    }
}

private const val PIE_SLICE_GAP_DEG = 2.4f

private fun buildPieSliceLayouts(active: List<PieSlice>, total: Double): List<PieSliceLayout> {
    val sliceGapDeg = if (active.size > 1) PIE_SLICE_GAP_DEG else 0f
    var startAngle = -90f
    return active.map { slice ->
        var sweep = (360f * (slice.valueMinor / total)).toFloat()
        if (sliceGapDeg > 0f && sweep > sliceGapDeg) {
            startAngle += sliceGapDeg / 2f
            sweep -= sliceGapDeg
        }
        val layout = PieSliceLayout(
            slice = slice,
            percent = slice.valueMinor / total * 100.0,
            startAngleDeg = startAngle,
            midAngleDeg = startAngle + sweep / 2f,
            sweepDeg = sweep,
        )
        startAngle += sweep + sliceGapDeg / 2f
        layout
    }
}

private fun normalizeAngleDegrees(angle: Float): Float {
    var a = angle % 360f
    if (a < 0f) a += 360f
    return a
}

private fun isAngleInSweep(tapAngle: Float, startAngle: Float, sweep: Float): Boolean {
    if (sweep <= 0f) return false
    val tap = normalizeAngleDegrees(tapAngle)
    val start = normalizeAngleDegrees(startAngle)
    val end = normalizeAngleDegrees(startAngle + sweep)
    return if (start <= end) {
        tap in start..end
    } else {
        tap >= start || tap <= end
    }
}

private data class AllocationPiePage(
    val title: String,
    val subtitle: String,
    val slices: List<PieSlice>,
)

@Composable
fun AllocationPieChartsPager(
    accounts: List<AccountWithCalculation>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    val pages = remember(accounts) {
        dashboardAllocationPages.map { spec ->
            val slices = when (spec.grouping) {
                AllocationPieGrouping.BY_ACCOUNT ->
                    pieSlicesForAccounts(accounts, spec.includeTypes)
                AllocationPieGrouping.BY_TYPE ->
                    pieSlicesByType(
                        accounts = accounts,
                        includeTypes = spec.includeTypes,
                        typeOrder = spec.typeOrder.orEmpty(),
                    )
                AllocationPieGrouping.BY_ASSET_CLASS ->
                    pieSlicesByAssetClass(
                        accounts = accounts,
                        assetClassOrder = spec.assetClassOrder.orEmpty(),
                    )
            }
            AllocationPiePage(
                title = when (spec.grouping) {
                    AllocationPieGrouping.BY_TYPE -> "Allocation by type"
                    AllocationPieGrouping.BY_ASSET_CLASS -> "Allocation by asset class"
                    AllocationPieGrouping.BY_ACCOUNT -> "Allocation"
                },
                subtitle = spec.subtitle,
                slices = slices,
            )
        }
    }
    val current = pages[pagerState.currentPage.coerceIn(0, pages.lastIndex)]

    Column(modifier = modifier.fillMaxWidth()) {
        Text(current.title, style = MaterialTheme.typography.titleLarge)
        Text(
            current.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val swipeHint = when {
            pages[pagerState.currentPage].title.contains("by asset class") ->
                "Swipe for by-type view →"
            pages[pagerState.currentPage].title.contains("by type") ->
                "Swipe for by asset class →"
            else -> "Swipe for by type view →"
        }
        Text(
            swipeHint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(12.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            PortfolioPieChart(slices = pages[page].slices)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            pages.forEachIndexed { index, _ ->
                val selected = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.25f),
                        ),
                )
            }
        }
    }
}

@Composable
fun PortfolioPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
) {
    val active = slices.filter { it.valueMinor > 0L }
    if (active.isEmpty()) {
        Text(
            "Add accounts to see allocation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val total = active.sumOf { it.valueMinor }.toDouble().coerceAtLeast(1.0)
    val sliceLayouts = remember(active, total) { buildPieSliceLayouts(active, total) }
    var selectedIndex by remember(active) { mutableIntStateOf(-1) }
    LaunchedEffect(active) { selectedIndex = -1 }

    val popProgress by animateFloatAsState(
        targetValue = if (selectedIndex >= 0) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "piePop",
    )
    val explodeDp = 14.dp
    val density = LocalDensity.current
    val explodePx = with(density) { explodeDp.toPx() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(contentAlignment = Alignment.Center) {
            val chartSize = min(maxWidth, 300.dp)
            Box(modifier = Modifier.size(chartSize)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(sliceLayouts) {
                            detectTapGestures { tap ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val rel = tap - center
                                val radius = minOf(size.width, size.height) / 2f * 0.98f
                                if (rel.getDistance() > radius) {
                                    selectedIndex = -1
                                    return@detectTapGestures
                                }
                                val tapAngle = Math.toDegrees(
                                    atan2(rel.y.toDouble(), rel.x.toDouble()),
                                ).toFloat()
                                val hitIndex = sliceLayouts.indexOfFirst { layout ->
                                    isAngleInSweep(tapAngle, layout.startAngleDeg, layout.sweepDeg)
                                }
                                selectedIndex = when {
                                    hitIndex < 0 -> -1
                                    hitIndex == selectedIndex -> -1
                                    else -> hitIndex
                                }
                            }
                        },
                ) {
                    val radius = size.minDimension / 2f * 0.98f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val arcTopLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)
                    sliceLayouts.forEachIndexed { index, layout ->
                        if (layout.sweepDeg <= 0f) return@forEachIndexed
                        val isSelected = index == selectedIndex
                        val explode = if (isSelected) popProgress * explodePx else 0f
                        val midRad = Math.toRadians(layout.midAngleDeg.toDouble())
                        val shift = Offset(
                            x = cos(midRad).toFloat() * explode,
                            y = sin(midRad).toFloat() * explode,
                        )
                        val sliceAlpha = when {
                            selectedIndex < 0 -> 1f
                            isSelected -> 1f
                            else -> 0.45f
                        }
                        withTransform({ translate(shift.x, shift.y) }) {
                            drawArc(
                                color = layout.slice.color.copy(alpha = sliceAlpha),
                                startAngle = layout.startAngleDeg,
                                sweepAngle = layout.sweepDeg,
                                useCenter = true,
                                topLeft = arcTopLeft,
                                size = arcSize,
                            )
                        }
                    }
                }
                sliceLayouts.forEachIndexed { index, layout ->
                    if (layout.sweepDeg < 4f) return@forEachIndexed
                    val isSelected = index == selectedIndex
                    val explode = if (isSelected) popProgress else 0f
                    PieSliceInnerLabel(
                        name = layout.slice.label,
                        percent = layout.percent,
                        midAngleDeg = layout.midAngleDeg,
                        sweepDeg = layout.sweepDeg,
                        chartRadius = chartSize / 2,
                        labelColor = layout.slice.color.onPieSliceLabel(),
                        explodeFraction = explode,
                        explodeDistance = explodeDp,
                        emphasized = isSelected,
                    )
                }
            }
        }
        if (selectedIndex in sliceLayouts.indices) {
            val selected = sliceLayouts[selectedIndex]
            Spacer(Modifier.height(12.dp))
            PieSliceSelectionCard(
                slice = selected.slice,
                percent = selected.percent,
                popProgress = popProgress,
            )
        }
    }
}

@Composable
private fun PieSliceSelectionCard(
    slice: PieSlice,
    percent: Double,
    popProgress: Float,
) {
    val scale by animateFloatAsState(
        targetValue = 0.92f + popProgress * 0.08f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = (4.dp * popProgress),
        shadowElevation = (8.dp * popProgress),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size((10.dp * scale))
                    .clip(CircleShape)
                    .background(slice.color),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    slice.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    formatInrMinor(slice.valueMinor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${"%.1f".format(US, percent)}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = slice.color,
            )
        }
    }
}

private data class PieSliceLayout(
    val slice: PieSlice,
    val percent: Double,
    val startAngleDeg: Float,
    val midAngleDeg: Float,
    val sweepDeg: Float,
)

@Composable
private fun BoxScope.PieSliceInnerLabel(
    name: String,
    percent: Double,
    midAngleDeg: Float,
    sweepDeg: Float,
    chartRadius: Dp,
    labelColor: Color,
    explodeFraction: Float,
    explodeDistance: Dp,
    emphasized: Boolean,
) {
    val rad = Math.toRadians(midAngleDeg.toDouble())
    val labelRadius = chartRadius * 0.55f
    val explodeX = explodeDistance * explodeFraction * cos(rad).toFloat()
    val explodeY = explodeDistance * explodeFraction * sin(rad).toFloat()
    val offsetX = labelRadius * cos(rad).toFloat() + explodeX
    val offsetY = labelRadius * sin(rad).toFloat() + explodeY
    val maxNameChars = when {
        emphasized -> 18
        sweepDeg >= 45f -> 14
        sweepDeg >= 25f -> 10
        sweepDeg >= 15f -> 6
        else -> 0
    }
    val displayName = if (maxNameChars > 0) {
        name.take(maxNameChars).let { if (it.length < name.length) "$it…" else it }
    } else {
        ""
    }

    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = offsetX, y = offsetY),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (displayName.isNotEmpty()) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = "${"%.1f".format(US, percent)}%",
            style = if (emphasized) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = labelColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun MonthlyInterestBarChart(
    points: List<MonthlyInterestPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Text(
            "Chart appears once interest accrues",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    var selectedIndex by remember(points) { mutableIntStateOf(-1) }
    val barColors = listOf(TealLight, PpfColor, SsaColor, PfColor, Color(0xFF00838F))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(12.dp)
                    .pointerInput(points) {
                        fun selectAt(x: Float) {
                            val barWidth = size.width / points.size.coerceAtLeast(1)
                            selectedIndex = (x / barWidth).toInt().coerceIn(0, points.lastIndex)
                        }
                        detectTapGestures { offset -> selectAt(offset.x) }
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            selectAt(change.position.x)
                        }
                    },
            ) {
                val maxInterest = points.maxOf { it.interestMinor }.coerceAtLeast(1L).toFloat()
                Canvas(Modifier.matchParentSize()) {
                    val barWidth = size.width / points.size
                    points.forEachIndexed { index, point ->
                        val heightRatio = point.interestMinor / maxInterest
                        val barHeight = size.height * 0.72f * heightRatio
                        val color = barColors[index % barColors.size]
                        drawRoundRect(
                            color = if (index == selectedIndex) color else color.copy(alpha = 0.78f),
                            topLeft = Offset(index * barWidth + barWidth * 0.18f, size.height - barHeight - 20f),
                            size = Size(barWidth * 0.64f, barHeight),
                            cornerRadius = CornerRadius(10f, 10f),
                        )
                    }
                }
            }
            if (selectedIndex in points.indices) {
                val p = points[selectedIndex]
                val label = "${p.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${p.yearMonth.year}"
                Text(
                    "$label · ${formatInrMinor(p.interestMinor)}${if (p.isProvisional) " (est.)" else ""}",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text(
                    "Drag across bars for monthly interest",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

fun colorForInvestmentType(type: InvestmentType): Color = when (type) {
    InvestmentType.PPF -> PpfColor
    InvestmentType.SSA -> SsaColor
    InvestmentType.PF -> PfColor
    InvestmentType.FD -> FdColor
    InvestmentType.RD -> RdColor
    InvestmentType.STOCKS -> Color(0xFF7C3AED)
    InvestmentType.METALS -> Color(0xFFD97706)
    InvestmentType.REAL_ESTATE -> Color(0xFF059669)
    InvestmentType.NPS -> Color(0xFF2563EB)
}
