package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DriverBlue
import com.example.ui.theme.DriverOrange

@Composable
fun QuickLocationAction(
    title: String,
    address: String,
    landmark: String = "",
    latitude: Double,
    longitude: Double,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_location_action_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DriverBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "Location Pin",
                            tint = DriverBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (landmark.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📍 $landmark",
                    style = MaterialTheme.typography.labelSmall,
                    color = DriverOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large Action Button "فتح الموقع"
            Button(
                onClick = {
                    openExternalMap(context, latitude, longitude, title)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("open_location_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DriverBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Open Maps",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DriverStrings.actionOpenLocation(isArabic),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Standard Android intent to open native Maps without mock simulation.
 */
fun openExternalMap(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to browser Google Maps
        try {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Location: $lat, $lng", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Standard Android intent to dial phone.
 */
fun callPhoneNumber(context: Context, phone: String) {
    try {
        val dialUri = Uri.parse("tel:${phone.replace(" ", "")}")
        val dialIntent = Intent(Intent.ACTION_DIAL, dialUri)
        dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(dialIntent)
    } catch (e: Exception) {
        Toast.makeText(context, phone, Toast.LENGTH_SHORT).show()
    }
}
