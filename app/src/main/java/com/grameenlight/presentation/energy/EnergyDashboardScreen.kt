package com.grameenlight.presentation.energy

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.grameenlight.presentation.common.theme.PrimaryGreen
import com.grameenlight.presentation.common.theme.SecondaryGreen
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnergyDashboardScreen(
    viewModel: EnergyViewModel = hiltViewModel()
) {
    val state by viewModel.energyState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(rememberScrollState())
    ) {
        // Dark Green Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(listOf(SecondaryGreen, PrimaryGreen)),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
                .padding(top = 24.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Energy Saved", color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                    IconButton(onClick = { shareEnergySummary(context, state) }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%.1f", state.kwhSaved),
                        color = Color.White,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " kWh",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Village Rank: #${state.villageRanking}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImpactCard(
                    icon = Icons.Default.Park,
                    title = "${(state.kwhSaved / 10).toInt()} Trees",
                    subtitle = "Equivalent planted",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ImpactCard(
                    icon = Icons.Default.Eco,
                    title = "${String.format(Locale.US, "%.1f", state.co2Saved)} kg",
                    subtitle = "CO2 reduced",
                    color = Color(0xFFEF9F27),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Resolution Efficiency", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = state.resolvedPercentage / 100f,
                            modifier = Modifier.size(160.dp),
                            color = PrimaryGreen,
                            strokeWidth = 12.dp,
                            trackColor = Color(0xFFF1F1F1)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${state.resolvedPercentage.toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Resolved", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("Total Issues", state.totalBurningDayCount.toString())
                        StatItem("Resolved", state.resolvedCount.toString())
                        StatItem("Efficiency", "${state.resolvedPercentage.toInt()}%")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Monthly Trend Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Monthly Performance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                description.isEnabled = false
                                setDrawGridBackground(false)
                                axisRight.isEnabled = false
                                legend.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                axisLeft.setDrawGridLines(false)
                                xAxis.textColor = AndroidColor.GRAY
                                axisLeft.textColor = AndroidColor.GRAY
                            }
                        },
                        update = { barChart ->
                            if (state.monthlyTrend.isNotEmpty()) {
                                val entries = state.monthlyTrend.mapIndexed { index, pair ->
                                    BarEntry(index.toFloat(), pair.second)
                                }
                                val dataSet = BarDataSet(entries, "kWh").apply {
                                    color = AndroidColor.parseColor("#1D9E75")
                                    setDrawValues(false)
                                    highLightAlpha = 0
                                }
                                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(state.monthlyTrend.map { it.first })
                                barChart.xAxis.granularity = 1f
                                barChart.data = BarData(dataSet)
                                barChart.invalidate()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ImpactCard(icon: ImageVector, title: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private fun shareEnergySummary(context: Context, state: EnergyState) {
    val shareText = """
        🌱 Grameen-Light Energy Summary
        
        We saved ${String.format(Locale.US, "%.1f", state.kwhSaved)} kWh this month!
        Equivalent to reducing ${String.format(Locale.US, "%.2f", state.co2Saved)} kg of CO2.
        
        Our village rank: #${state.villageRanking}
        Let's keep Grameen-Light green! 🌍
    """.trimIndent()
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(intent, "Share Energy Summary"))
}
