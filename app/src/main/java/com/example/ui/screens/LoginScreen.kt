package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.model.VehicleType
import com.example.ui.auth.GoogleSignInHelper
import com.example.ui.localization.DriverStrings
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DriverEmerald
import com.example.ui.theme.DriverOrange
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LoginScreen(
    isArabic: Boolean,
    onLoginSuccess: (phone: String, vehicleType: VehicleType) -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneInput by remember { mutableStateOf("+966 55 123 4567") }
    var selectedVehicle by remember { mutableStateOf(VehicleType.MOTORCYCLE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("driver_login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Language toggle top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .testTag("login_language_toggle")
                    .clickable(onClick = onToggleLanguage)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DriverStrings.languageToggle(isArabic),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(DriverOrange),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_driver_logo),
                contentDescription = "Driver Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = DriverStrings.appName(isArabic),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = DriverStrings.loginSubtitle(isArabic),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Phone Input
        Text(
            text = DriverStrings.phoneLabel(isArabic),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("driver_phone_input"),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = DriverOrange
                )
            },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DriverOrange,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Vehicle Selector
        Text(
            text = DriverStrings.vehicleSelectLabel(isArabic),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VehicleOption(
                type = VehicleType.MOTORCYCLE,
                name = if (isArabic) "دراجة 🛵" else "Moto 🛵",
                isSelected = selectedVehicle == VehicleType.MOTORCYCLE,
                onClick = { selectedVehicle = VehicleType.MOTORCYCLE },
                modifier = Modifier.weight(1f)
            )
            VehicleOption(
                type = VehicleType.CAR,
                name = if (isArabic) "سيارة 🚗" else "Car 🚗",
                isSelected = selectedVehicle == VehicleType.CAR,
                onClick = { selectedVehicle = VehicleType.CAR },
                modifier = Modifier.weight(1f)
            )
            VehicleOption(
                type = VehicleType.BICYCLE,
                name = if (isArabic) "هوائية 🚲" else "Bike 🚲",
                isSelected = selectedVehicle == VehicleType.BICYCLE,
                onClick = { selectedVehicle = VehicleType.BICYCLE },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Large Primary Start Shift CTA
        Button(
            onClick = {
                onLoginSuccess(phoneInput, selectedVehicle)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .testTag("start_shift_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = DriverOrange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = DriverStrings.startShiftBtn(isArabic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Google Sign-In with Credential Manager
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val credentialManager = remember { CredentialManager.create(context) }
        var isGoogleLoading by remember { mutableStateOf(false) }

        Button(
            onClick = {
                isGoogleLoading = true
                val activity = context as? Activity
                if (activity != null) {
                    GoogleSignInHelper.launchGoogleSignIn(
                        activity = activity,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isGoogleLoading = false
                            val user = Firebase.auth.currentUser
                            val userPhone = user?.phoneNumber?.ifBlank { phoneInput } ?: phoneInput
                            onLoginSuccess(userPhone, selectedVehicle)
                        },
                        onAuthError = {
                            isGoogleLoading = false
                        },
                        onAuthCancelled = {
                            isGoogleLoading = false
                        },
                        scope = coroutineScope
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("google_sign_in_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = !isGoogleLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isGoogleLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = DriverOrange
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google",
                        tint = DriverOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "تسجيل الدخول عبر Google" else "Sign in with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Demo Captain Button
        OutlinedButton(
            onClick = {
                phoneInput = "+966 55 123 4567"
                selectedVehicle = VehicleType.MOTORCYCLE
                onLoginSuccess(phoneInput, selectedVehicle)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("quick_demo_login_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = DriverStrings.quickDemoLogin(isArabic) + " (Captain Ahmed)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun VehicleOption(
    type: VehicleType,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("vehicle_option_${type.name.lowercase()}")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, DriverOrange, RoundedCornerShape(14.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ),
        color = if (isSelected) DriverOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) DriverOrange else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
