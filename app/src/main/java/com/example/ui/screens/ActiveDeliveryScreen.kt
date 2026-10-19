package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStatus
import com.example.data.model.PaymentMethod
import com.example.ui.components.ActiveDeliveryTracker
import com.example.ui.components.callPhoneNumber
import com.example.ui.components.openExternalMap
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DriverBlue
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange

@Composable
fun ActiveDeliveryScreen(
    order: DeliveryOrder?,
    isArabic: Boolean,
    onAdvanceDeliveryStep: () -> Unit,
    onConfirmDelivered: () -> Unit,
    onCancelDelivery: (String) -> Unit,
    isConfirmDialogVisible: Boolean,
    isCancelDialogVisible: Boolean,
    onDismissConfirmDialog: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onOpenCancelDialog: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (order == null) {
        // No active delivery
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .testTag("empty_active_delivery_screen"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DriverEmerald,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = DriverStrings.noActiveDelivery(isArabic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DriverStrings.stayOnlineTip(isArabic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = DriverOrange)
                    ) {
                        Text(text = DriverStrings.navHome(isArabic), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("active_delivery_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Leave space for fixed giant bottom CTA
        ) {
            // Header: Order #, Earnings, Progress Tracker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
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
                        Column {
                            Text(
                                text = "${DriverStrings.orderNumberLabel(isArabic)} #${order.orderNumber}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = order.status.getDisplayName(isArabic),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DriverOrange
                            )
                        }

                        // Earnings Pill
                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "+${String.format("%.2f", order.driverEarnings)} ${order.currency}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-step progress bar
                    ActiveDeliveryTracker(
                        status = order.status,
                        isArabic = isArabic
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Target Card based on Current State
            val isPickupPhase = order.status == DeliveryStatus.ASSIGNED

            if (isPickupPhase) {
                // Focus: STORE / PICKUP
                StoreMissionCard(
                    order = order,
                    isArabic = isArabic,
                    onOpenMap = {
                        openExternalMap(
                            context = context,
                            lat = order.store.latitude,
                            lng = order.store.longitude,
                            label = order.store.getDisplayName(isArabic)
                        )
                    },
                    onCallStore = {
                        callPhoneNumber(context, order.store.phone)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                // Focus: CUSTOMER / DROPOFF
                CustomerMissionCard(
                    order = order,
                    isArabic = isArabic,
                    onOpenMap = {
                        openExternalMap(
                            context = context,
                            lat = order.deliveryAddress.latitude,
                            lng = order.deliveryAddress.longitude,
                            label = order.customer.name
                        )
                    },
                    onCallCustomer = {
                        callPhoneNumber(context, order.customer.phone)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Items & Summary
            OrderSummaryCard(
                order = order,
                isArabic = isArabic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Info: Other Destination reference
            if (isPickupPhase) {
                // Reference where it will be dropped off next
                DestinationPreviewPill(
                    title = if (isArabic) "وجهة التسليم التالية (العميل):" else "Next Destination (Customer):",
                    address = "${order.customer.name} - ${order.deliveryAddress.getDisplayArea(isArabic)}",
                    isArabic = isArabic,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                // Reference where it was picked up
                DestinationPreviewPill(
                    title = if (isArabic) "تم الاستلام من:" else "Picked up from:",
                    address = order.store.getDisplayName(isArabic),
                    isArabic = isArabic,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Assignment can only be released before pickup; later issues must go through support.
            if (order.status == DeliveryStatus.ASSIGNED) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = onOpenCancelDialog,
                    modifier = Modifier.testTag("report_delivery_issue_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.cancelIssueBtn(isArabic),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            }
        }

        // Fixed Bottom Sticky Bar with GIANT Primary CTA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("active_delivery_bottom_bar"),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val ctaLabel = order.status.getPrimaryActionLabel(isArabic) ?: ""
            val ctaColor = when (order.status) {
                DeliveryStatus.ASSIGNED -> DriverOrange
                DeliveryStatus.PICKED_UP -> DriverBlue
                DeliveryStatus.ON_THE_WAY -> DriverEmerald
                else -> DriverOrange
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = onAdvanceDeliveryStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("active_delivery_giant_cta_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ctaColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ctaLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Final Delivery Completion
    if (isConfirmDialogVisible) {
        val isCash = order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY
        AlertDialog(
            onDismissRequest = onDismissConfirmDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DriverEmerald,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = DriverStrings.confirmDeliveryTitle(isArabic),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = DriverStrings.confirmDeliveryMsg(isArabic))
                    if (isCash) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "⚠️ ${DriverStrings.cashToCollect(isArabic)}: ${String.format("%.2f", order.orderTotal)} ${order.currency}",
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelivered,
                    colors = ButtonDefaults.buttonColors(containerColor = DriverEmerald),
                    modifier = Modifier.testTag("confirm_delivered_dialog_button")
                ) {
                    Text(text = DriverStrings.confirmBtn(isArabic), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirmDialog) {
                    Text(text = DriverStrings.backBtn(isArabic))
                }
            }
        )
    }

    // Cancel / Issue Dialog
    if (isCancelDialogVisible) {
        AlertDialog(
            onDismissRequest = onDismissCancelDialog,
            icon = {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = DriverStrings.cancelIssueBtn(isArabic),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isArabic)
                        "هل تواجه مشكلة تحول دون إكمال التوصيل (العميل لا يرد / المتجر مغلق)؟"
                    else
                        "Are you experiencing an issue preventing delivery completion (customer unreachable / store closed)?"
                )
            },
            confirmButton = {
                Button(
                    onClick = { onCancelDelivery("Driver reported issue") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_cancel_issue_dialog_button")
                ) {
                    Text(text = DriverStrings.confirmBtn(isArabic), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancelDialog) {
                    Text(text = DriverStrings.backBtn(isArabic))
                }
            }
        )
    }
}

@Composable
private fun StoreMissionCard(
    order: DeliveryOrder,
    isArabic: Boolean,
    onOpenMap: () -> Unit,
    onCallStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_store_mission_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Target Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DriverOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = DriverOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DriverStrings.pickupPoint(isArabic),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DriverOrange
                        )
                    }
                }

                Text(
                    text = "📍 ${order.distanceKm} ${if (isArabic) "كم" else "km"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Store Name & Address
            Text(
                text = order.store.getDisplayName(isArabic),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = order.store.getDisplayAddress(isArabic),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Store pickup notes
            if (order.store.getInstructions(isArabic).isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 ${order.store.getInstructions(isArabic)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two Large Action Buttons: Open Maps & Call Store
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Open Location
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("active_open_store_map_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverBlue
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.actionOpenLocation(isArabic),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Call Store
                OutlinedButton(
                    onClick = onCallStore,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("active_call_store_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DriverOrange
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.actionCallStore(isArabic),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DriverOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerMissionCard(
    order: DeliveryOrder,
    isArabic: Boolean,
    onOpenMap: () -> Unit,
    onCallCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCash = order.paymentMethod == PaymentMethod.CASH_ON_DELIVERY

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_customer_mission_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Target Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DriverEmerald.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DriverEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = DriverStrings.dropoffPoint(isArabic),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = DriverEmerald
                        )
                    }
                }

                Text(
                    text = "📍 ${order.distanceKm} ${if (isArabic) "كم" else "km"}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Customer Name & Full Address
            Text(
                text = order.customer.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = order.deliveryAddress.getFullAddress(isArabic),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Customer delivery notes
            if (order.deliveryAddress.getNotes(isArabic).isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📝 ${order.deliveryAddress.getNotes(isArabic)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Notice Pill
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (isCash) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCash) DriverStrings.cashToCollect(isArabic) else DriverStrings.prepaidNotice(isArabic),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCash) Color(0xFF92400E) else Color(0xFF065F46)
                    )

                    if (isCash) {
                        Text(
                            text = "${String.format("%.2f", order.orderTotal)} ${order.currency}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Open Maps & Call Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Open Location
                Button(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(52.dp)
                        .testTag("active_open_customer_map_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverBlue
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.actionOpenLocation(isArabic),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Call Customer
                OutlinedButton(
                    onClick = onCallCustomer,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("active_call_customer_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = DriverEmerald
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.actionCallCustomer(isArabic),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DriverEmerald
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryCard(
    order: DeliveryOrder,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_order_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DriverStrings.itemsSummary(isArabic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${order.totalItemsCount} ${if (isArabic) "أصناف" else "items"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.quantity}x ${item.getDisplayName(isArabic)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.getDisplayNotes(isArabic).isNotBlank()) {
                        Text(
                            text = "(${item.getDisplayNotes(isArabic)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationPreviewPill(
    title: String,
    address: String,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = address,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
