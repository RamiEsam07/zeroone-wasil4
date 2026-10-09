package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.example.data.model.DriverStatus
import com.example.data.model.DriverUser
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DriverOrange
import com.example.ui.theme.StatusAvailableColor
import com.example.ui.theme.StatusBusyColor
import com.example.ui.theme.StatusOfflineColor

@Composable
fun DriverTopBar(
    driver: DriverUser,
    driverStatus: DriverStatus,
    isArabic: Boolean,
    onStatusClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSimulateNewOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("driver_top_bar"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Driver info & avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DriverOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = "Driver Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = driver.getDisplayName(isArabic),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${DriverStrings.driverTag(isArabic)} • ${driver.vehiclePlate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick Action buttons (Language, Simulate offer)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Simulate new incoming offer button
                    IconButton(
                        onClick = onSimulateNewOrder,
                        modifier = Modifier
                            .testTag("simulate_order_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Simulate Order",
                            tint = DriverOrange
                        )
                    }

                    // Language toggle button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .testTag("language_toggle_button")
                            .clickable(onClick = onToggleLanguage)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DriverStrings.languageToggle(isArabic),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Status Pill (Touch target >= 48dp, high contrast outdoor control)
            val (statusColor, statusBg, statusDot) = when (driverStatus) {
                DriverStatus.AVAILABLE -> Triple(
                    Color(0xFF065F46),
                    Color(0xFFD1FAE5),
                    StatusAvailableColor
                )
                DriverStatus.BUSY -> Triple(
                    Color(0xFF92400E),
                    Color(0xFFFEF3C7),
                    StatusBusyColor
                )
                DriverStatus.OFFLINE -> Triple(
                    Color(0xFF334155),
                    Color(0xFFE2E8F0),
                    StatusOfflineColor
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_pill_button")
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onStatusClick),
                color = statusBg,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(statusDot)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = driverStatus.getDisplayName(isArabic),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Text(
                        text = if (isArabic) "تغيير الحالة ▾" else "Change ▾",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }
        }
    }
}
