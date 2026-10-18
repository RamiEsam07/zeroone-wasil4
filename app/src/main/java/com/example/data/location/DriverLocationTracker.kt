package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

/**
 * Foreground-only driver location publisher. The caller controls when tracking starts;
 * it should run only while the driver has explicitly marked themselves available.
 */
class DriverLocationTracker(
    context: Context,
    private val onLocation: (latitude: Double, longitude: Double) -> Unit,
) {
    private val appContext = context.applicationContext
    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocation(location.latitude, location.longitude)
        }

        @Deprecated("Deprecated by Android; retained for API 24 compatibility")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
    }

    fun hasLocationPermission(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            appContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start(): Boolean {
        if (!hasLocationPermission()) return false
        stop()

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        ).filter { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (providers.isEmpty()) return false

        var subscribed = false
        providers.forEach { provider ->
            subscribed = runCatching {
                locationManager.requestLocationUpdates(provider, 30_000L, 50f, listener)
                true
            }.getOrDefault(false) || subscribed
        }

        providers.mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }?.let { onLocation(it.latitude, it.longitude) }

        return subscribed
    }

    fun stop() {
        runCatching { locationManager.removeUpdates(listener) }
    }
}
