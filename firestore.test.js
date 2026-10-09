const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DRIVER_ALICE = "driver_alice_123";
const DRIVER_BOB = "driver_bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles or orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(DRIVER_ALICE).get());
  await assertFails(unauthDb.collection("orders").get());
});

test("Driver: can create and read their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(DRIVER_ALICE).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(DRIVER_ALICE).set({
      userId: DRIVER_ALICE,
      name: "Captain Ahmed",
      phone: "+966551234567",
      status: "AVAILABLE",
      vehicleType: "MOTORCYCLE",
      vehiclePlate: "ABC 1234",
      rating: 4.9,
      totalDeliveries: 100,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(DRIVER_ALICE).get());
});

test("Driver: cannot access or modify another driver's profile", async () => {
  const bobDb = testEnv.authenticatedContext(DRIVER_BOB).firestore();
  await assertFails(bobDb.collection("users").doc(DRIVER_ALICE).get());
  await assertFails(
    bobDb.collection("users").doc(DRIVER_ALICE).set({
      userId: DRIVER_ALICE,
      name: "Attacker",
      phone: "+966550000000",
      status: "AVAILABLE",
      createdAt: new Date(),
    })
  );
});

test("Driver: can accept available delivery order and transition status", async () => {
  // Seed available order without driverId
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_101").set({
      orderNumber: "ORD-101",
      storeName: "Shawarma King",
      deliveryAddress: "Olaya, Riyadh",
      status: "ASSIGNED",
      orderTotal: 50.0,
      driverEarnings: 20.0,
      driverId: null,
      paymentMethod: "PREPAID",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(DRIVER_ALICE).firestore();

  // Alice accepts order
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_101").update({
      driverId: DRIVER_ALICE,
      status: "ASSIGNED",
      updatedAt: new Date(),
    })
  );

  // Alice transitions status to PICKED_UP
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_101").update({
      status: "PICKED_UP",
      updatedAt: new Date(),
    })
  );

  // Bob cannot modify Alice's order
  const bobDb = testEnv.authenticatedContext(DRIVER_BOB).firestore();
  await assertFails(
    bobDb.collection("orders").doc("order_101").update({
      status: "DELIVERED",
      updatedAt: new Date(),
    })
  );
});
