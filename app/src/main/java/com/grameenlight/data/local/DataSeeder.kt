package com.grameenlight.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.database.FirebaseDatabase
import com.grameenlight.domain.model.Pole
import com.grameenlight.domain.model.PoleStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.grameenlight.data.local.PoleDao
import com.grameenlight.data.local.entity.toEntity
import org.json.JSONArray
import java.io.InputStream
import java.nio.charset.Charset
import kotlinx.coroutines.tasks.await
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class DataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseDatabase: FirebaseDatabase,
    private val poleDao: PoleDao
) {
    companion object {
        private val IS_SEEDED = booleanPreferencesKey("is_seeded")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch_v2")
    }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    suspend fun seedDataIfNecessary() {
        val prefs = context.dataStore.data.first()
        val isFirstLaunch = prefs[IS_FIRST_LAUNCH] ?: true
        
        if (isFirstLaunch) {
            // FIX 1.3: Sign out once on first launch to clear old sessions
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            context.dataStore.edit { it[IS_FIRST_LAUNCH] = false }
        }

        val isSeeded = prefs[IS_SEEDED] ?: false
        val isRoomEmpty = poleDao.getAllPoles().isEmpty()

        if (!isSeeded || isRoomEmpty) {
            try {
                // FIX 2: GPS seeding
                val location = if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                    } catch (e: Exception) { null }
                } else null

                val baseLat = location?.latitude ?: 20.5937
                val baseLng = location?.longitude ?: 78.9629

                // Clear old data
                poleDao.clearAllPoles()
                firebaseDatabase.getReference("poles").removeValue().await()

                val streetNames = listOf(
                    "Main Road", "Temple Street", "School Lane", "Market Road", 
                    "Church Road", "River Bank Road", "Station Road", 
                    "Hospital Lane", "Panchayat Office Road", "Farmers Lane"
                )

                val statuses = mutableListOf<PoleStatus>().apply {
                    repeat(12) { add(PoleStatus.WORKING) }
                    repeat(4) { add(PoleStatus.FUSED) }
                    repeat(3) { add(PoleStatus.BURNING_DAY) }
                    repeat(1) { add(PoleStatus.UNKNOWN) }
                    shuffle()
                }

                val polesList = mutableListOf<Pole>()
                for (i in 1..20) {
                    val latOffset = (Random.nextDouble() - 0.5) * 0.005
                    val lngOffset = (Random.nextDouble() - 0.5) * 0.005
                    val poleId = "POLE-${String.format("%03d", i)}"
                    
                    polesList.add(Pole(
                        poleId = poleId,
                        latitude = baseLat + latOffset,
                        longitude = baseLng + lngOffset,
                        streetName = streetNames[i % streetNames.size],
                        currentStatus = statuses[i - 1],
                        lastUpdatedAt = System.currentTimeMillis(),
                        lastReportedBy = "SYSTEM_GPS_SEED"
                    ))
                }
                
                poleDao.upsertPoles(polesList.map { it.toEntity() })
                val polesRef = firebaseDatabase.getReference("poles")
                val polesMap = polesList.associateBy({ it.poleId }, { it })
                polesRef.updateChildren(polesMap as Map<String, Any>).await()
                
                context.dataStore.edit { it[IS_SEEDED] = true }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadJSONFromAsset(fileName: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
