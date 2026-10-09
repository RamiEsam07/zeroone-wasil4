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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.PaymentMethod
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange
import com.example.ui.theme.StatusAvailableColor
import com.example.ui.theme.StatusBusyColor
import com.example.ui.theme.StatusDeliveredColor
import com.example.ui.theme.StatusOnTheWayColor

@Composable
fun DeliveryCard(
    order: DeliveryOrder,
    isArabic: Boolean,
    onPrimaryAction: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAvailableOffer: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("delivery_card_${order.id}")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Order Number + Driver Earnings Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Status Badge
                    val (badgeText, badgeBg, badgeTextColor) = if (isAvailableOffer) {
                        Triple(
                            if (isArabic) "عرض متاح" else "New Offer",
                            Color(0xFFFEF3C7),
                            Color(0xFF92400E)
                        )
                    } else {
                        when (order.status) {
                            DeliveryStatus.ASSIGNED -> Triple(
                                if (isArabic) "استلام من المتجر" else "Pickup",
                                Color(0xFFFEF3C7),
                                Color(0xFF92400E)
                            )
                            DeliveryStatus.PICKED_UP -> Triple(
                                if (isArabic) "تم الاستلام" else "Picked Up",
                                Color(0xFFDBEAFE),
                                Color(0xFF1E40AF)
                            )
                            DeliveryStatus.ON_THE_WAY -> Triple(
                                if (isArabic) "في الطريق" else "On The Way",
                                Color(0xFFDBEAFE),
                                Color(0xFF1E40AF)
                            )
                            DeliveryStatus.DELIVERED -> Triple(
                                if (isArabic) "مكتمل" else "Delivered",
                                Color(0xFFD1FAE5),
                                Color(0xFF065F46)
                            )
                            DeliveryStatus.CANCELLED -> Triple(
                                if (isArabic) "ملغي" else "Cancelled",
                                Color(0xFFFEE2E2),
                                Color(0xFF991B1B)
                            )
                        }
                    }

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Driver Earnings Highlight (Crucial for courier motivation)
                Surface(
                    color = Color(0xFFD1FAE5),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+${String.format("%.2f", order.driverEarnings)} ${order.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pickup & Dropoff Route Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(12.dp)
            ) {
                // Store / Pickup
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DriverOrange.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Store",
                            tint = DriverOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.store.getDisplayName(isArabic),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = order.store.getDisplayAddress(isArabic),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Connecting route line
                Row(
                    modifier = Modifier.padding(start = 13.dp, top = 2.dp, bottom = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(14.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                }

                // Customer / Dropoff
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DriverEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Dropoff",
                            tint = DriverEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = order.deliveryAddress.getDisplayArea(isArabic),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = order.deliveryAddress.getFullAddress(isArabic),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Info: Distance, Items, Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📍 ${order.distanceKm} ${if (isArabic) "كم" else "km"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "⏱ ~${order.estimatedDurationMin} ${if (isArabic) "دقيقة" else "min"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "📦 ${order.totalItemsCount} ${if (isArabic) "أصناف" else "items"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment pill
                val isCash = order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY
                Text(
                    text = if (isCash) (if (isArabic) "💵 كاش" else "💵 Cash") else (if (isArabic) "💳 إلكتروني" else "💳 Paid"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCash) Color(0xFFD97706) else Color(0xFF059669)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large, Obvious Primary CTA Button (Height >= 52dp)
            val primaryLabel = if (isAvailableOffer) {
                DriverStrings.actionAcceptDelivery(isArabic)
            } else {
                order.status.getPrimaryActionLabel(isArabic) ?: DriverStrings.actionAcceptDelivery(isArabic)
            }

            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("delivery_card_action_button_${order.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAvailableOffer) DriverEmerald else DriverOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = primaryLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
