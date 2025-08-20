package com.fightcalendar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.size
import androidx.hilt.navigation.compose.hiltViewModel
import com.fightcalendar.app.R
import com.fightcalendar.app.domain.model.FreeTimeSlot
import com.fightcalendar.app.ui.components.HourlyData
import com.fightcalendar.app.ui.components.HourlyTimeline
import com.fightcalendar.app.ui.components.TimelineLegend
import com.fightcalendar.app.ui.theme.FightCalendarTheme
import com.fightcalendar.app.ui.theme.getCategoryColor



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    viewModel: DayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    FightCalendarTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        DateNavigationHeader(
                            // TODO: Use actual date from ViewModel state
                            currentDate = "8月20日（火）",
                            onPreviousDay = { /* TODO */ },
                            onNextDay = { /* TODO */ },
                            onToday = { /* TODO */ }
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.action_refresh)
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.action_settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with win rate, streak, and total time
                item {
                    HeaderSummary(
                        winRate = state.winRate,
                        streakDays = state.streakDays,
                        totalScreenTime = state.totalScreenTime
                    )
                }
                
                // 24-hour Timeline (directly under header)
                item {
                    if (state.hourlyData.isEmpty()) {
                        EmptyTimelineCard()
                    } else {
                        EnhancedTimelineSection(
                            hourlyData = state.hourlyData,
                            onHourClick = { hour ->
                                // TODO: Show hour details in bottom sheet
                            }
                        )
                    }
                }
                
                // Free Time Card
                item {
                    EnhancedFreeTimeCard(
                        freeTimeSlots = state.freeTimeSlots,
                        onBlockSlot = { slot ->
                            viewModel.blockFreeTimeSlot(slot)
                        }
                    )
                }
                
                // 4 Category Schedule
                item {
                    CategoryScheduleCard(
                        // TODO: Add category usage data to state
                        categories = emptyMap()
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderSummary(
    winRate: Int,
    streakDays: Int,
    totalScreenTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Win Rate
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.today_winrate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.winrate_format, winRate),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (winRate >= 50) {
                    Color(0xFFFF7A59) // アクセント色
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
        
        // Streak Days
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.streak_days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Total Screen Time
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.total_screentime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = totalScreenTime,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EnhancedTimelineSection(
    hourlyData: List<HourlyData>,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.timeline_24h),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        // Enhanced timeline with events and free time overlays
        HourlyTimeline(
            hours = hourlyData,
            onHourClick = onHourClick
        )
        
        // Time labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0", "6", "12", "18", "24").forEach { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        TimelineLegend()
    }
}

@Composable
private fun TimelineSection(
    hourlyData: List<HourlyData>,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.timeline_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            HourlyTimeline(
                hours = hourlyData,
                onHourClick = onHourClick
            )
            
            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0", "6", "12", "18", "24").forEach { time ->
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            TimelineLegend()
        }
    }
}

@Composable
private fun FreeTimeSection(
    freeTimeSlots: List<FreeTimeSlot>,
    onBlockSlot: (FreeTimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.todays_free_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (freeTimeSlots.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_free_slots),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                freeTimeSlots.forEach { slot ->
                    FreeTimeSlotItem(
                        slot = slot,
                        onBlock = { onBlockSlot(slot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeTimeSlotItem(
    slot: FreeTimeSlot,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${slot.getFormattedTimeRange()} (${slot.durationMinutes}分)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        Button(
            onClick = onBlock,
            modifier = Modifier.padding(start = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(text = stringResource(R.string.action_block_now))
        }
    }
}

@Composable
private fun EnhancedFreeTimeCard(
    freeTimeSlots: List<FreeTimeSlot>,
    onBlockSlot: (FreeTimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.todays_free_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (freeTimeSlots.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_free_slots),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Total free time
                val totalMinutes = freeTimeSlots.sumOf { it.durationMinutes }
                val totalHours = totalMinutes / 60
                val remainingMinutes = totalMinutes % 60
                
                Text(
                    text = stringResource(R.string.free_time_total_format, totalHours, remainingMinutes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Next free slot
                val nextSlot = freeTimeSlots.firstOrNull { it.durationMinutes >= 60 }
                if (nextSlot != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.next_free_slot),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${nextSlot.getFormattedTimeRange()}（${nextSlot.durationMinutes}分）",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Button(
                                    onClick = { onBlockSlot(nextSlot) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.action_block),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryScheduleCard(
    categories: Map<String, Long>, // TODO: Use Category enum
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.todays_schedule),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // 4 Category fixed display
            val categoryData = listOf(
                Triple("仕事", Color(0xFF2BB673), 4 * 60 * 60 * 1000L),
                Triple("学習", Color(0xFF2F6DF6), 2 * 60 * 60 * 1000L),
                Triple("娯楽・SNS", Color(0xFFFF7A59), 1 * 60 * 60 * 1000L),
                Triple("ツール", Color(0xFF7E57C2), 30 * 60 * 1000L)
            )
            
            categoryData.forEach { (name, color, timeMs) ->
                val hours = timeMs / (60 * 60 * 1000)
                val minutes = (timeMs % (60 * 60 * 1000)) / (60 * 1000)
                val timeText = if (hours > 0) "${hours}時間${minutes}分" else "${minutes}分"
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (timeMs > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateNavigationHeader(
    currentDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.action_prev_day),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            TextButton(
                onClick = onToday,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            
            IconButton(
                onClick = onNextDay,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.action_next_day),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

