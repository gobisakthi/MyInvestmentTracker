package com.personal.investmenttracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personal.investmenttracker.ui.theme.GoldSoft
import com.personal.investmenttracker.ui.theme.TealDark
import com.personal.investmenttracker.ui.theme.TealLight
import com.personal.investmenttracker.ui.theme.TealPrimary

@Composable
fun PremiumHeroCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(TealPrimary, TealDark, Color(0xFF065F46)),
                ),
            )
            .padding(20.dp),
    ) {
        Column(content = content)
    }
}

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface,
        content = { Column(Modifier.padding(18.dp), content = content) },
    )
}

@Composable
fun PremiumAccentChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(GoldSoft.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TealDark,
        )
    }
}
