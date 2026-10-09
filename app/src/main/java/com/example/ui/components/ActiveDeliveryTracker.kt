package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.theme.DriverBlue
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange

@Composable
fun ActiveDeliveryTracker(
    status: DeliveryStatus,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    // Stage 1: Pickup from Store (active when ASSIGNED)
    // Stage 2: On The Way to Customer (active when PICKED_UP or ON_THE_WAY)
    // Stage 3: Delivered (active when DELIVERED)
    val stage = when (status) {
        DeliveryStatus.ASSIGNED -> 1
        DeliveryStatus.PICKED_UP -> 2
        DeliveryStatus.ON_THE_WAY -> 2
        DeliveryStatus.DELIVERED -> 3
        DeliveryStatus.CANCELLED -> 0
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_delivery_tracker"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Step 1: Pickup
                StepNode(
                    stepNumber = 1,
                    title = if (isArabic) "١. الاستلام" else "1. Pickup",
                    subtitle = if (isArabic) "من المتجر" else "From Store",
                    isCompleted = stage > 1,
                    isCurrent = stage == 1,
                    modifier = Modifier.weight(1f)
                )

                // Divider 1-2
                StepDivider(isCompleted = stage >= 2)

                // Step 2: On The Way
                StepNode(
                    stepNumber = 2,
                    title = if (isArabic) "٢. التوصيل" else "2. On Way",
                    subtitle = if (isArabic) "للعميل" else "To Customer",
                    isCompleted = stage > 2,
                    isCurrent = stage == 2,
                    modifier = Modifier.weight(1f)
                )

                // Divider 2-3
                StepDivider(isCompleted = stage >= 3)

                // Step 3: Delivered
                StepNode(
                    stepNumber = 3,
                    title = if (isArabic) "٣. التسليم" else "3. Delivered",
                    subtitle = if (isArabic) "إتمام الطلب" else "Complete",
                    isCompleted = stage == 3,
                    isCurrent = stage == 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StepNode(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (circleBg, iconTint) = when {
            isCompleted -> Pair(DriverEmerald, Color.White)
            isCurrent -> Pair(DriverOrange, Color.White)
            else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(circleBg)
                .then(
                    if (isCurrent) Modifier.border(2.dp, DriverOrange.copy(alpha = 0.5f), CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$stepNumber",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = iconTint
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isCurrent) DriverOrange else MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun StepDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(28.dp)
            .height(3.dp)
            .background(
                if (isCompleted) DriverEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(2.dp)
            )
            .padding(horizontal = 2.dp)
    )
}
