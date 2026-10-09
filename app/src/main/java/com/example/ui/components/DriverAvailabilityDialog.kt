package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.DriverStatus
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StatusAvailableColor
import com.example.ui.theme.StatusBusyColor
import com.example.ui.theme.StatusOfflineColor

@Composable
fun DriverAvailabilityDialog(
    currentStatus: DriverStatus,
    isArabic: Boolean,
    onStatusSelected: (DriverStatus) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("driver_availability_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DriverStrings.quickStatusControl(isArabic),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_availability_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (isArabic)
                        "حدد حالتك الحالية لتنظيم استقبال وإسناد طلبات التوصيل"
                    else
                        "Select your status to manage delivery requests assignment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AVAILABLE Option
                StatusOptionCard(
                    status = DriverStatus.AVAILABLE,
                    title = if (isArabic) "متاح (جاهز للعمل)" else "Available (Online)",
                    description = if (isArabic) "استلام عروض الطلبات الجديدة فور صدورها" else "Receive new incoming delivery offers immediately",
                    color = StatusAvailableColor,
                    bgColor = Color(0xFFD1FAE5),
                    textColor = Color(0xFF065F46),
                    isSelected = currentStatus == DriverStatus.AVAILABLE,
                    onClick = { onStatusSelected(DriverStatus.AVAILABLE) },
                    testTag = "status_available_option"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // BUSY Option
                StatusOptionCard(
                    status = DriverStatus.BUSY,
                    title = if (isArabic) "مشغول بمهمة" else "Busy on Task",
                    description = if (isArabic) "أنت بصدد استلام أو توصيل طلب حالي" else "Currently picking up or delivering an order",
                    color = StatusBusyColor,
                    bgColor = Color(0xFFFEF3C7),
                    textColor = Color(0xFF92400E),
                    isSelected = currentStatus == DriverStatus.BUSY,
                    onClick = { onStatusSelected(DriverStatus.BUSY) },
                    testTag = "status_busy_option"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // OFFLINE Option
                StatusOptionCard(
                    status = DriverStatus.OFFLINE,
                    title = if (isArabic) "غير متصل (استراحة)" else "Offline (Break)",
                    description = if (isArabic) "إيقاف استقبال أي طلبات مؤقتاً لأخذ راحة" else "Stop incoming offers temporarily to take a break",
                    color = StatusOfflineColor,
                    bgColor = Color(0xFFE2E8F0),
                    textColor = Color(0xFF334155),
                    isSelected = currentStatus == DriverStatus.OFFLINE,
                    onClick = { onStatusSelected(DriverStatus.OFFLINE) },
                    testTag = "status_offline_option"
                )
            }
        }
    }
}

@Composable
private fun StatusOptionCard(
    status: DriverStatus,
    title: String,
    description: String,
    color: Color,
    bgColor: Color,
    textColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, color, RoundedCornerShape(16.dp))
                else Modifier
            ),
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.85f)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
