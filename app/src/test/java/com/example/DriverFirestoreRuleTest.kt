package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class DriverFirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun driverProfile_authenticatedUser_canWriteAndReadOwnProfile() = runBlocking {
    val uid = signInTestUser("captain_ahmed@test.com")
    val userRef = firestore.collection("users").document(uid)

    val profileData = mapOf(
      "userId" to uid,
      "name" to "Captain Ahmed",
      "phone" to "+966551234567",
      "status" to "AVAILABLE",
      "vehicleType" to "MOTORCYCLE",
      "createdAt" to FieldValue.serverTimestamp()
    )

    withTimeout(5000L) {
      userRef.set(profileData).await()
    }

    val snapshot = withTimeout(5000L) {
      userRef.get().await()
    }
    assertTrue(snapshot.exists())
    assertEquals("Captain Ahmed", snapshot.getString("name"))
  }

  @Test
  fun driverProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice_driver@test.com")
    val aliceDoc = firestore.collection("users").document(aliceUid)
    withTimeout(5000L) {
      aliceDoc.set(
        mapOf(
          "userId" to aliceUid,
          "name" to "Alice Driver",
          "phone" to "+966551112233",
          "status" to "AVAILABLE",
          "createdAt" to FieldValue.serverTimestamp()
        )
      ).await()
    }

    // Sign in as Bob
    signInTestUser("bob_driver@test.com")
    val bobDocRef = firestore.collection("users").document(aliceUid)

    try {
      withTimeout(5000L) {
        bobDocRef.get().await()
      }
      fail("Expected FirebaseFirestoreException but succeeded")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val ordersRef = firestore.collection("orders")
    try {
      withTimeout(5000L) {
        ordersRef.get().await()
      }
      fail("Expected FirebaseFirestoreException but succeeded")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }
}
