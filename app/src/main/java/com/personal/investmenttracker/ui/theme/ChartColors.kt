package com.personal.investmenttracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.personal.investmenttracker.domain.model.InvestmentType

/**
 * Curated allocation palette — distinct hues per account, grouped by investment type.
 */
private val ppfPieShades = listOf(
    Color(0xFF1E40AF),
    Color(0xFF2563EB),
    Color(0xFF0284C7),
    Color(0xFF0E7490),
)

private val ssaPieShades = listOf(
    Color(0xFF6D28D9),
    Color(0xFF9333EA),
    Color(0xFFC026D3),
    Color(0xFFBE185D),
)

private val pfPieShades = listOf(
    Color(0xFFC2410C),
    Color(0xFFEA580C),
    Color(0xFFD97706),
    Color(0xFFCA8A04),
)

/** Used when many accounts exceed per-type shade lists. */
private val extraPieShades = listOf(
    Color(0xFF059669),
    Color(0xFF0D9488),
    Color(0xFF4F46E5),
    Color(0xFF7C2D12),
    Color(0xFF1D4ED8),
    Color(0xFFDB2777),
)

fun pieSliceColor(type: InvestmentType, indexAmongType: Int): Color {
    val family = when (type) {
        InvestmentType.PPF -> ppfPieShades
        InvestmentType.SSA -> ssaPieShades
        InvestmentType.PF -> pfPieShades
    }
    return family[indexAmongType % family.size]
}

/** Assign colors so neighbouring slices (by draw order) stay visually distinct. */
fun assignPremiumPieColors(
    slices: List<PieSliceColorInput>,
): List<Color> {
    if (slices.isEmpty()) return emptyList()
    val result = MutableList(slices.size) { Color.Unspecified }
    val used = mutableSetOf<Color>()
    slices.forEachIndexed { index, input ->
        val candidates = buildList {
            add(pieSliceColor(input.type, input.indexAmongType))
            addAll(extraPieShades)
            addAll(
                when (input.type) {
                    InvestmentType.PPF -> ssaPieShades + pfPieShades
                    InvestmentType.SSA -> pfPieShades + ppfPieShades
                    InvestmentType.PF -> ppfPieShades + ssaPieShades
                },
            )
        }.distinct()
        val prev = result.getOrNull(index - 1)
        val chosen = candidates.firstOrNull { candidate ->
            candidate != prev && candidate !in used
        } ?: candidates.firstOrNull { it != prev } ?: candidates.first()
        result[index] = chosen
        used.add(chosen)
    }
    return result
}

data class PieSliceColorInput(
    val type: InvestmentType,
    val indexAmongType: Int,
)

fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}

fun Color.onPieSliceLabel(): Color =
    if (luminance() > 0.62f) Color(0xFF0F172A) else Color.White
