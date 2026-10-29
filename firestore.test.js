const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_sby_01";
const BOB_UID = "bob_sby_02";
const CHARLIE_UID = "charlie_sby_03";

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

function serverNow() {
  return new Date();
}

test("Unauthenticated caller is rejected across all collections", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("incidents").where("visibility", "==", "SURABAYA_PUBLIC").get());
  await assertFails(unauth.collection("events").where("status", "==", "APPROVED").get());
  await assertFails(unauth.collection("culinary").where("visibility", "==", "SURABAYA_PUBLIC").get());
  await assertFails(unauth.collection("nearbyProfiles").where("isDiscoverable", "==", true).get());
  await assertFails(unauth.collection("chatRooms").where("roomType", "==", "PUBLIC").get());
});

test("Private user account & bookmarks enforce strict owner isolation (PII Split Test)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = serverNow();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Ayu Suroboyo",
      district: "Gubeng",
      bio: "Pecinta Rawon",
      searchRadiusKm: 10,
      isDiscoverable: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob cannot read Alice's private user document
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());

  // Shadow update with ghost field must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Ayu",
      ghostPrivilege: true,
      updatedAt: serverNow(),
    })
  );

  // Bookmarks owner isolation
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("bm_1").set({
      userId: ALICE_UID,
      targetId: "kul_1",
      targetType: "KULINER",
      title: "Rawon Setan Embong Malang",
      subtitle: "Genteng, Surabaya",
      district: "Genteng",
      createdAt: serverNow(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").where("userId", "==", ALICE_UID).get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").where("userId", "==", ALICE_UID).get()
  );
});

test("Event Surabaya: regular user can only create PENDING, admin can approve to APPROVED", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobAdminDb = testEnv.authenticatedContext(BOB_UID, {
    email: "nabilalailatus12@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext(CHARLIE_UID, {
    email: "nabilalailatus12@gmail.com",
    email_verified: false,
  }).firestore();

  const baseEvent = {
    proposerId: ALICE_UID,
    proposerName: "Ayu",
    title: "Festival Rujak Uleg Kembang Jepun",
    category: "FESTIVAL",
    eventDateText: "Minggu, 15 Nov 2026",
    timeText: "08.00 - 14.00 WIB",
    venueName: "Kya-Kya Kembang Jepun",
    district: "Pabean Cantian",
    address: "Jl. Kembang Jepun, Surabaya",
    lat: -7.2392,
    lng: 112.7406,
    priceLabel: "Gratis",
    description: "Festival budaya kuliner legendaris warga Surabaya.",
    imageKey: "event_festival",
    adminNote: "",
    interestedCount: 0,
    interestedUids: [],
    createdAt: serverNow(),
    updatedAt: serverNow(),
  };

  // Regular user cannot create directly as APPROVED
  await assertFails(
    aliceDb.collection("events").doc("ev_1").set({
      ...baseEvent,
      status: "APPROVED",
    })
  );

  // Regular user CAN propose as PENDING
  await assertSucceeds(
    aliceDb.collection("events").doc("ev_1").set({
      ...baseEvent,
      status: "PENDING",
    })
  );

  // Unverified spoofed admin email CANNOT approve
  await assertFails(
    spoofAdminDb.collection("events").doc("ev_1").update({
      status: "APPROVED",
      adminNote: "Spoof",
      updatedAt: serverNow(),
    })
  );

  // Verified admin CAN approve
  await assertSucceeds(
    bobAdminDb.collection("events").doc("ev_1").update({
      status: "APPROVED",
      adminNote: "Disetujui untuk tayang di agenda kota.",
      updatedAt: serverNow(),
    })
  );

  // Public query for APPROVED events succeeds
  await assertSucceeds(
    aliceDb.collection("events").where("status", "==", "APPROVED").get()
  );

  // Also test Admin Mode toggle via /admins/{uid} for a regular Google user
  await assertSucceeds(
    aliceDb.collection("events").doc("ev_2").set({
      ...baseEvent,
      status: "PENDING",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      userId: ALICE_UID,
      enabled: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("events").doc("ev_2").update({
      status: "APPROVED",
      adminNote: "Disetujui lewat Mode Admin profil",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Direct Chat Rooms & Messages enforce participant isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Pre-creation existence check on non-existent room succeeds cleanly
  await assertSucceeds(aliceDb.collection("chatRooms").doc("dm_alice_bob").get());

  await assertSucceeds(
    aliceDb.collection("chatRooms").doc("dm_alice_bob").set({
      roomType: "DIRECT",
      title: "Ayu & Budi",
      subtitle: "Gubeng • Wonokromo",
      participantUids: [ALICE_UID, BOB_UID],
      participantNames: "Ayu, Budi",
      lastMessage: "Halo rek!",
      lastSenderName: "Ayu",
      createdBy: ALICE_UID,
      createdAt: serverNow(),
      updatedAt: serverNow(),
    })
  );

  // Charlie (non-participant) cannot read direct chat room
  await assertFails(charlieDb.collection("chatRooms").doc("dm_alice_bob").get());

  // Bob (participant) can read direct chat room and send message
  await assertSucceeds(bobDb.collection("chatRooms").doc("dm_alice_bob").get());
  await assertSucceeds(
    bobDb.collection("chatRooms").doc("dm_alice_bob").collection("messages").doc("msg_1").set({
      roomId: "dm_alice_bob",
      roomType: "DIRECT",
      participantUids: [ALICE_UID, BOB_UID],
      senderId: BOB_UID,
      senderName: "Budi",
      text: "Halo Ayu, salam kenal dari Wonokromo!",
      createdAt: serverNow(),
    })
  );

  // Charlie cannot read Alice & Bob's message or list messages without Charlie's own participant filter
  await assertFails(
    charlieDb.collection("chatRooms").doc("dm_alice_bob").collection("messages").doc("msg_1").get()
  );
  await assertFails(
    charlieDb.collection("chatRooms").doc("dm_alice_bob").collection("messages")
      .where("roomType", "==", "DIRECT")
      .where("participantUids", "array-contains", ALICE_UID)
      .get()
  );
  await assertFails(
    charlieDb.collection("chatRooms").doc("dm_alice_bob").collection("messages").doc("msg_hack").set({
      roomId: "dm_alice_bob",
      roomType: "DIRECT",
      participantUids: [ALICE_UID, CHARLIE_UID],
      senderId: CHARLIE_UID,
      senderName: "Charlie",
      text: "Intruder",
      createdAt: serverNow(),
    })
  );
});
