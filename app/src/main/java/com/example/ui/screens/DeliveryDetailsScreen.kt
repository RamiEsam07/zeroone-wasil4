package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.PaymentMethod
import com.example.ui.components.callPhoneNumber
import com.example.ui.components.openExternalMap
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DriverBlue
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange

@Composable
fun DeliveryDetailsScreen(
    order: DeliveryOrder?,
    isArabic: Boolean,
    onAcceptDelivery: (String) -> Unit,
    onNavigateToActiveDelivery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (order == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Order not found", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val isAvailableOffer = order.status == DeliveryStatus.ASSIGNED && order.assignedAt == null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("delivery_details_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("details_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${DriverStrings.orderNumberLabel(isArabic)} #${order.orderNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Driver Earnings Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = DriverStrings.driverEarnings(isArabic),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "${String.format("%.2f", order.driverEarnings)} ${order.currency}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "📍 ${order.distanceKm} ${if (isArabic) "كم" else "km"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = "⏱ ~${order.estimatedDurationMin} ${if (isArabic) "دقيقة" else "min"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Store Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = DriverOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DriverStrings.pickupPoint(isArabic),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                openExternalMap(
                                    context,
                                    order.store.latitude,
                                    order.store.longitude,
                                    order.store.getDisplayName(isArabic)
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Map",
                                tint = DriverBlue
                            )
                        }
                    }

                    Text(
                        text = order.store.getDisplayName(isArabic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = order.store.getDisplayAddress(isArabic),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (order.store.getInstructions(isArabic).isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 ${order.store.getInstructions(isArabic)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DriverOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DriverEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DriverStrings.dropoffPoint(isArabic),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                openExternalMap(
                                    context,
                                    order.deliveryAddress.latitude,
                                    order.deliveryAddress.longitude,
                                    order.customer.name
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Map",
                                tint = DriverBlue
                            )
                        }
                    }

                    Text(
                        text = "${order.customer.name} (${order.deliveryAddress.getDisplayArea(isArabic)})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = order.deliveryAddress.getFullAddress(isArabic),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (order.deliveryAddress.getNotes(isArabic).isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📝 ${order.deliveryAddress.getNotes(isArabic)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items breakdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = DriverStrings.itemsSummary(isArabic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.getDisplayName(isArabic)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${String.format("%.2f", item.totalPrice)} ${order.currency}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = DriverStrings.orderTotal(isArabic),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${String.format("%.2f", order.orderTotal)} ${order.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DriverOrange
                        )
                    }
                }
            }
        }

        // Bottom CTA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (order.status.isActive) {
                    Button(
                        onClick = onNavigateToActiveDelivery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("details_go_to_active_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DriverOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (isArabic) "متابعة الطلب في الشاشة النشطة" else "Go to Active Mission",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                    }
                } else if (order.status == DeliveryStatus.DELIVERED) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFD1FAE5),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = DriverStrings.statusDelivered(isArabic),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { onAcceptDelivery(order.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("details_accept_delivery_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DriverEmerald),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = DriverStrings.actionAcceptDelivery(isArabic),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
