package com.example

import android.os.Bundle
import android.Manifest
import android.widget.Toast
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.SupabaseDriverRepository
import com.example.data.location.DriverLocationTracker
import com.example.data.model.DriverStatus
import com.example.data.supabase.SupabaseRoleAuthenticator
import com.example.data.supabase.SupabaseSession
import com.example.data.supabase.SupabaseSessionProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.DriverAppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DriverViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Shared Firebase-backed repository instance for driver app
        val repository = SupabaseDriverRepository()

        setContent {
            val viewModel = remember { DriverViewModel(repository) }
            val isArabic by viewModel.isArabic.collectAsState()
            val navController = rememberNavController()

            // Dynamic RTL layout direction based on Arabic toggle
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SupabaseDriverAuthGate(viewModel = viewModel) {
                            DriverAppNavigation(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun SupabaseDriverAuthGate(
    viewModel: DriverViewModel,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authenticator = remember(context) { SupabaseRoleAuthenticator(context, "driver") }
    val scope = rememberCoroutineScope()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val driverStatus by viewModel.driverStatus.collectAsState()
    val locationTracker = remember(context, viewModel) {
        DriverLocationTracker(context) { latitude, longitude ->
            scope.launch { runCatching { viewModel.publishDriverLocation(latitude, longitude) } }
        }
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            if (!locationTracker.start()) {
                Toast.makeText(context, "فعّل خدمة الموقع لإرسال موقعك أثناء التوفر.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "لم يُسمح بالموقع؛ لن يعمل الإسناد التلقائي حسب القرب.", Toast.LENGTH_LONG).show()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    var session by remember { mutableStateOf<SupabaseSession?>(null) }
    var restoring by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    suspend fun activate(candidate: SupabaseSession): Boolean {
        SupabaseSessionProvider.session = candidate
        val result = viewModel.loginWithSupabase()
        if (result.isSuccess) {
            session = candidate
            errorMessage = null
            return true
        }
        authenticator.signOut(candidate)
        SupabaseSessionProvider.session = null
        session = null
        errorMessage = result.exceptionOrNull()?.message ?: "تعذر تحميل ملف المندوب."
        return false
    }

    LaunchedEffect(authenticator) {
        val restored = authenticator.restore()
        if (restored.isSuccess) restored.getOrNull()?.let { activate(it) }
        else errorMessage = restored.exceptionOrNull()?.message
        restoring = false
    }

    LaunchedEffect(isLoggedIn, session) {
        if (session != null && !isLoggedIn) {
            authenticator.signOut(session)
            SupabaseSessionProvider.session = null
            session = null
        }
    }

    LaunchedEffect(session, isLoggedIn, driverStatus) {
        if (session != null && isLoggedIn && driverStatus != DriverStatus.OFFLINE) {
            if (locationTracker.hasLocationPermission()) {
                if (!locationTracker.start()) {
                    Toast.makeText(context, "فعّل خدمة الموقع لإرسال موقعك أثناء التوفر.", Toast.LENGTH_LONG).show()
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        } else {
            locationTracker.stop()
        }
    }

    DisposableEffect(lifecycleOwner, locationTracker, session, isLoggedIn, driverStatus) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                locationTracker.stop()
            } else if (
                event == Lifecycle.Event.ON_START &&
                session != null && isLoggedIn && driverStatus != DriverStatus.OFFLINE &&
                locationTracker.hasLocationPermission()
            ) {
                locationTracker.start()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            locationTracker.stop()
        }
    }

    LaunchedEffect(session, isLoggedIn) {
        if (session != null && isLoggedIn) {
            while (true) {
                delay(15_000)
                runCatching { viewModel.refreshSupabaseData() }
            }
        }
    }

    if (session != null && isLoggedIn) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("مندوب وصّل", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("الدخول متاح لحسابات المندوبين المفعّلة فقط.")
            Spacer(Modifier.height(24.dp))
            if (restoring || loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    enabled = authenticator.isConfigured() && activity != null,
                    onClick = {
                        val hostActivity = activity ?: return@Button
                        scope.launch {
                            loading = true
                            errorMessage = null
                            val result = authenticator.signIn(hostActivity)
                            if (result.isSuccess) activate(result.getOrThrow())
                            else errorMessage = result.exceptionOrNull()?.message ?: "تعذر تسجيل الدخول."
                            loading = false
                        }
                    },
                ) {
                    Text("المتابعة باستخدام Google")
                }
            }
            if (!authenticator.isConfigured()) {
                Spacer(Modifier.height(12.dp))
                Text("أكمل إعداد Supabase وGoogle OAuth في local.properties أولاً.", color = MaterialTheme.colorScheme.error)
            }
            if (!errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
