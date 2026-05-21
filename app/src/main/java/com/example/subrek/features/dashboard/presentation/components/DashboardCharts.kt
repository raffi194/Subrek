package com.example.subrek.features.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subrek.core.theme.*
import com.example.subrek.features.subscription.domain.model.Subscription

val ChartColors = listOf(Blue500, Amber500, Rose500, Emerald500, Indigo500)

@Composable
fun DonutChartCategories(subscriptions: List<Subscription>) {
    val cycleSpends = subscriptions.groupBy { it.billingCycle.name }.mapValues { entry ->
        entry.value.sumOf { sub ->
            when (sub.billingCycle.name) {
                "YEARLY" -> sub.price / 12.0
                else -> sub.price
            }
        }
    }
    
    val totalSpend = cycleSpends.values.sum()
    if (totalSpend == 0.0) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, shape = RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                cycleSpends.values.forEachIndexed { index, spend ->
                    val sweepAngle = ((spend / totalSpend) * 360f).toFloat()
                    drawArc(
                        color = ChartColors.getOrElse(index) { Slate400 },
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = "Siklus",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cycleSpends.entries.forEachIndexed { index, entry ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(ChartColors.getOrElse(index) { Slate400 })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.key,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    val percentage = ((entry.value / totalSpend) * 100).toInt()
                    Text(
                        text = "$percentage%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChartMethods(subscriptions: List<Subscription>) {
    val methodCounts = subscriptions.groupBy { it.paymentMethod }.mapValues { it.value.size }
    val totalCount = methodCounts.values.sum().toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, shape = RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Breakdown Metode Pembayaran",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        methodCounts.entries.forEachIndexed { index, entry ->
            val ratio = entry.value / totalCount

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = entry.key, fontSize = 12.sp, color = Slate400)
                    Text(text = "${entry.value} Layanan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue500)
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    drawRoundRect(
                        color = Slate950,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = ChartColors.getOrElse(index) { Blue500 },
                        size = size.copy(width = size.width * ratio),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }
        }
    }
}
