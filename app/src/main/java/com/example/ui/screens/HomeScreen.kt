package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
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
import com.example.data.model.DriverStatus
import com.example.data.model.DriverUser
import com.example.data.model.TodayDriverStats
import com.example.ui.components.DeliveryCard
import com.example.ui.components.DriverTopBar
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DriverBlue
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange

@Composable
fun HomeScreen(
    driver: DriverUser,
    driverStatus: DriverStatus,
    activeDelivery: DeliveryOrder?,
    availableOrders: List<DeliveryOrder>,
    todayStats: TodayDriverStats,
    isArabic: Boolean,
    onStatusClick: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSimulateOrder: () -> Unit,
    onNavigateToActiveDelivery: () -> Unit,
    onNavigateToAvailableDeliveries: () -> Unit,
    onOrderClick: (DeliveryOrder) -> Unit,
    onAcceptDelivery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("driver_home_screen")
    ) {
        // Sticky Header / Top Bar
        item {
            DriverTopBar(
                driver = driver,
                driverStatus = driverStatus,
                isArabic = isArabic,
                onStatusClick = onStatusClick,
                onToggleLanguage = onToggleLanguage,
                onSimulateNewOrder = onSimulateOrder
            )
        }

        // Today's Metrics Bar (Earnings, Completed Deliveries, Active Hours)
        item {
            TodayStatsSummaryBar(
                stats = todayStats,
                isArabic = isArabic,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Active Delivery Hero Card (If present)
        item {
            if (activeDelivery != null) {
                ActiveDeliveryHeroCard(
                    order = activeDelivery,
                    isArabic = isArabic,
                    onClick = onNavigateToActiveDelivery,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Available Delivery Requests Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = DriverOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${DriverStrings.availableOrdersTitle(isArabic)} (${availableOrders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (availableOrders.isNotEmpty()) {
                    Text(
                        text = DriverStrings.viewAll(isArabic),
                        style = MaterialTheme.typography.labelMedium,
                        color = DriverOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .testTag("view_all_available_orders")
                            .clickable(onClick = onNavigateToAvailableDeliveries)
                    )
                }
            }
        }

        // Available Deliveries list
        if (availableOrders.isEmpty()) {
            item {
                EmptyAvailableOrdersCard(
                    driverStatus = driverStatus,
                    isArabic = isArabic,
                    onGoOnline = onStatusClick,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(availableOrders.take(3), key = { it.id }) { order ->
                DeliveryCard(
                    order = order,
                    isArabic = isArabic,
                    isAvailableOffer = true,
                    onPrimaryAction = { onAcceptDelivery(order.id) },
                    onCardClick = { onOrderClick(order) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TodayStatsSummaryBar(
    stats: TodayDriverStats,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_stats_summary"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Earnings
            StatItem(
                label = DriverStrings.todayEarnings(isArabic),
                value = "${String.format("%.2f", stats.totalEarnings)} ${stats.currency}",
                icon = Icons.Default.LocalAtm,
                accentColor = DriverEmerald
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // Completed Deliveries
            StatItem(
                label = DriverStrings.todayCompleted(isArabic),
                value = "${stats.completedDeliveriesCount} ${if (isArabic) "طلب" else "orders"}",
                icon = Icons.Default.CheckCircle,
                accentColor = DriverOrange
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            // Active Hours
            StatItem(
                label = DriverStrings.activeHours(isArabic),
                value = "${stats.activeHours} ${if (isArabic) "س" else "h"}",
                icon = Icons.Default.Schedule,
                accentColor = DriverBlue
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActiveDeliveryHeroCard(
    order: DeliveryOrder,
    isArabic: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_delivery_hero_card")
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF1E293B), // Premium high contrast dark slate
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
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
                            .background(DriverOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DriverStrings.activeDeliveryTitle(isArabic),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DriverOrange
                    )
                }

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "#${order.orderNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current State & Target
            val stateTitle = when (order.status) {
                DeliveryStatus.ASSIGNED -> if (isArabic) "توجه إلى: ${order.store.getDisplayName(true)}" else "Go to: ${order.store.getDisplayName(false)}"
                DeliveryStatus.PICKED_UP, DeliveryStatus.ON_THE_WAY -> if (isArabic) "التوصيل إلى: ${order.deliveryAddress.getDisplayArea(true)}" else "Deliver to: ${order.deliveryAddress.getDisplayArea(false)}"
                else -> order.status.getDisplayName(isArabic)
            }

            Text(
                text = stateTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = order.status.getActionInstruction(isArabic),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Giant CTA inside hero card
            val nextCtaText = order.status.getPrimaryActionLabel(isArabic) ?: (if (isArabic) "فتح شاشة التوصيل" else "Open Mission")

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("hero_active_delivery_action_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DriverOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = nextCtaText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAvailableOrdersCard(
    driverStatus: DriverStatus,
    isArabic: Boolean,
    onGoOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("empty_available_orders_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBike,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = DriverStrings.noAvailableOrders(isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = DriverStrings.stayOnlineTip(isArabic),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (driverStatus == DriverStatus.OFFLINE) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onGoOnline,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverEmerald
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isArabic) "الاتصال بالإنترنت الآن" else "Go Online Now",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
