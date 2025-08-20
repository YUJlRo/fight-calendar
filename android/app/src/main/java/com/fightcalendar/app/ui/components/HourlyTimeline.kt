package com.fightcalendar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.fightcalendar.app.ui.theme.*

data class HourlyData(
    val hour: Int, // 0-23
    val category: String? = null,
    val usageMinutes: Int = 0,
    val hasEvents: Boolean = false,
    val hasFreeTime: Boolean = false
) {
    val hasUsage: Boolean get() = usageMinutes > 0
}

@Composable
fun HourlyTimeline(
    hours: List<HourlyData>,
    modifier: Modifier = Modifier,
    onHourClick: (Int) -> Unit = {}
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier) {
        // Main timeline bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            hours.forEach { hourData ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onHourClick(hourData.hour) }
                        .background(getHourColor(hourData))
                )
            }
        }
        
        // Overlay for events and free time
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val barWidth = size.width / 24f
            val barHeight = with(density) { 12.dp.toPx() }
            val overlayHeight = with(density) { 3.dp.toPx() }
            
            hours.forEachIndexed { index, hourData ->
                val startX = index * barWidth
                
                // Event overlay (top)
                if (hourData.hasEvents) {
                    drawRect(
                        color = WidgetEventOverlay,
                        topLeft = Offset(startX, 0f),
                        size = androidx.compose.ui.geometry.Size(barWidth, overlayHeight)
                    )
                }
                
                // Free time highlight (bottom)
                if (hourData.hasFreeTime) {
                    drawRect(
                        color = FreeTimeHighlight,
                        topLeft = Offset(startX, barHeight + overlayHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, overlayHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun getHourColor(hourData: HourlyData): Color {
    return when {
        hourData.category != null -> getCategoryColor(hourData.category)
        hourData.hasUsage -> MaterialTheme.colorScheme.outline
        else -> getUnusedColor()
    }
}

/**
 * Mini timeline for widgets - simplified version
 */
@Composable
fun MiniTimeline(
    hours: List<HourlyData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        hours.forEach { hourData ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(getHourColor(hourData))
            )
        }
    }
}

/**
 * Timeline legend showing categories
 */
@Composable
fun TimelineLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = CategoryWork,
            label = "仕事"
        )
        LegendItem(
            color = CategoryStudy,
            label = "学習"
        )
        LegendItem(
            color = CategoryEntertainment,
            label = "娯楽"
        )
        LegendItem(
            color = CategoryTools,
            label = "ツール"
        )
        LegendItem(
            color = getUnusedColor(),
            label = "未使用"
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}