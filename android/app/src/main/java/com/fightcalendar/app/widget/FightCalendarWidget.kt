package com.fightcalendar.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.fightcalendar.app.MainActivity
import com.fightcalendar.app.R

data class WidgetData(
    val winRate: Int,
    val totalScreenTime: String,
    val hourlyData: List<HourlyWidgetData>,
    val totalFreeTime: String,
    val nextFreeSlot: String
)

data class HourlyWidgetData(
    val hour: Int,
    val category: String? = null,
    val hasEvents: Boolean = false,
    val hasFreeTime: Boolean = false
)

class FightCalendarWidget : GlanceAppWidget() {
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            FightCalendarWidgetContent(context)
        }
    }
}

@Composable
private fun FightCalendarWidgetContent(context: Context) {
    // TODO: Load actual data from repository
    val mockData = WidgetData(
        winRate = 67,
        totalScreenTime = "5時間23分",
        hourlyData = (0..23).map { hour ->
            HourlyWidgetData(
                hour = hour,
                category = when {
                    hour in 6..8 -> "work"
                    hour in 13..17 -> "work"
                    hour in 19..21 -> "entertainment"
                    hour in 9..11 -> "study"
                    else -> null
                },
                hasEvents = hour in listOf(9, 14, 16),
                hasFreeTime = hour in listOf(10, 15, 18)
            )
        },
        totalFreeTime = "2時間45分",
        nextFreeSlot = "15:30–16:30（60分）"
    )
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF7FBFB))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Header with win rate and screen time
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Win Rate
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.today_winrate),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF063A3A)
                    )
                )
                Text(
                    text = "${mockData.winRate}%",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mockData.winRate >= 50) {
                            Color(0xFFFF7A59) // tertiary color
                        } else {
                            Color(0xFF063A3A)
                        }
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(16.dp))
            
            // Total Screen Time
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = context.getString(R.string.total_screentime),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color(0xFF063A3A)
                    )
                )
                Text(
                    text = mockData.totalScreenTime,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF063A3A)
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Mini Timeline
        Text(
            text = "24時間タイムライン",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF063A3A)
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        MiniTimelineWidget(hourlyData = mockData.hourlyData)
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Time labels
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            listOf("0", "6", "12", "18", "24").forEachIndexed { index, time ->
                if (index > 0) Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = time,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Free Time Section
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = context.getString(R.string.todays_free_time),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF063A3A)
                    )
                )
                Text(
                    text = mockData.totalFreeTime,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81CAC4)
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Next Free Slot
        if (mockData.nextFreeSlot.isNotEmpty()) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(Color(0x1F81CAC4))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = context.getString(R.string.next_free_slot),
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFF063A3A)
                        )
                    )
                    Text(
                        text = mockData.nextFreeSlot,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF063A3A)
                        )
                    )
                }
                
                // Block button
                Button(
                    text = context.getString(R.string.action_block_now),
                    onClick = actionRunCallback<BlockFreeTimeAction>(),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF81CAC4),
                        contentColor = Color.White
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Open app button
        Button(
            text = "アプリを開く",
            onClick = actionStartActivity<MainActivity>(),
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.White
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF81CAC4),
                contentColor = Color.White
            )
        )
    }
}

@Composable
private fun MiniTimelineWidget(
    hourlyData: List<HourlyWidgetData>,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        hourlyData.forEach { hourData ->
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .background(getHourColor(hourData))
            )
        }
    }
}

private fun getHourColor(hourData: HourlyWidgetData): Color {
    return when (hourData.category) {
        "work" -> Color(0xFF2BB673)
        "study" -> Color(0xFF2F6DF6)
        "entertainment" -> Color(0xFFFF7A59)
        "tools" -> Color(0xFF7E57C2)
        else -> Color(0xFFDFECEA) // unused/light theme
    }
}

class BlockFreeTimeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // TODO: Implement free time blocking logic
        // This would interact with the calendar repository to create a "Free" event
    }
}

class FightCalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FightCalendarWidget()
}