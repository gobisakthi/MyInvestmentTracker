package com.personal.investmenttracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.personal.investmenttracker.domain.model.InvestmentType

/**
 * Premium allocation palette — each slice gets a clearly different hue (yellow, green, orange, …).
 */
val premiumPiePalette = listOf(
    Color(0xFFEAB308), // gold / yellow
    Color(0xFF16A34A), // green
    Color(0xFFEA580C), // orange
    Color(0xFF9333EA), // purple
    Color(0xFF0891B2), // cyan
    Color(0xFFE11D48), // rose
    Color(0xFF4F46E5), // indigo
    Color(0xFF84CC16), // lime
    Color(0xFFDB2777), // pink
    Color(0xFF0D9488), // teal
    Color(0xFFF97316), // bright orange
    Color(0xFF2563EB), // blue
    Color(0xFFCA8A04), // amber
    Color(0xFF059669), // emerald
    Color(0xFF7C3AED), // violet
    Color(0xFFDC2626), // red
)

/** Spread hues around the wheel so neighbours rarely look alike. */
private const val PALETTE_STRIDE = 5

fun pieSliceColor(type: InvestmentType, indexAmongType: Int): Color {
    val typeSeed = type.ordinal * 2 + indexAmongType
    return premiumPiePalette[typeSeed % premiumPiePalette.size]
}

fun assignPremiumPieColors(
    slices: List<PieSliceColorInput>,
): List<Color> {
    if (slices.isEmpty()) return emptyList()
    val palette = premiumPiePalette
    val result = MutableList(slices.size) { Color.Unspecified }
    slices.forEachIndexed { index, input ->
        var pick = (index * PALETTE_STRIDE + input.type.ordinal) % palette.size
        var attempts = 0
        while (attempts < palette.size) {
            val candidate = palette[pick]
            val prev = result.getOrNull(index - 1)
            if (prev == null || !colorsTooSimilar(prev, candidate)) {
                result[index] = candidate
                break
            }
            pick = (pick + 1) % palette.size
            attempts++
        }
        if (result[index] == Color.Unspecified) {
            result[index] = palette[index % palette.size]
        }
    }
    return result
}

private fun colorsTooSimilar(a: Color, b: Color): Boolean {
    if (a == b) return true
    val dr = kotlin.math.abs(a.red - b.red)
    val dg = kotlin.math.abs(a.green - b.green)
    val db = kotlin.math.abs(a.blue - b.blue)
    return dr + dg + db < 0.18f
}

data class PieSliceColorInput(
    val type: InvestmentType,
    val indexAmongType: Int,
)

fun Color.luminance(): Float {
    return 0.2126f * red + 0.7152f * green + 0.0722f * blue
}

fun Color.onPieSliceLabel(): Color =
    if (luminance() > 0.58f) Color(0xFF0F172A) else Color.White
