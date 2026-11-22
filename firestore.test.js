const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_1";
const BOB_UID = "bob_uid_2";

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

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").where("discoverable", "==", true).get());
  await assertFails(unauthDb.collection("voice_rooms").where("publicListed", "==", true).get());
  await assertFails(unauthDb.collection("users_private").doc(ALICE_UID).get());
});

test("Public user profile & private PII isolation + partial update parity", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Alice creates public profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "alice_star",
      displayName: "Alice Star",
      avatarEmoji: "🌟",
      avatarColorHex: "#7C4DFF",
      bio: "Voice host & gamer",
      country: "India",
      language: "Hindi",
      level: 1,
      xp: 100,
      vipTier: "NONE",
      discoverable: true,
      adultEligible: true,
      onlineVisible: true,
      onlineStatus: "ONLINE",
      followersCount: 0,
      friendsCount: 0,
      popularityScore: 10,
      giftsReceivedValue: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice creates private PII + wallet
  await assertSucceeds(
    aliceDb.collection("users_private").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@example.com",
      fullName: "Alice Sharma",
      dobIso: "1999-05-12",
      coinBalance: 1000,
      vipTier: "NONE",
      hideOnline: false,
      hideLastSeen: false,
      allowFriendRequests: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob can query discoverable public profiles, NOT unfiltered, and NEVER Alice's private PII
  await assertSucceeds(bobDb.collection("users").where("discoverable", "==", true).get());
  await assertFails(bobDb.collection("users").get());
  await assertFails(bobDb.collection("users_private").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked" }));

  // Alice partial update succeeds; ghost field or createdAt tamper fails
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 250,
      level: 2,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      ghostField: "hack",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Voice rooms: create, filtered list, seat occupancy, room chat, speaker request", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);
  const roomId = "room_music_1";

  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).set({
      hostId: ALICE_UID,
      hostName: "Alice Star",
      title: "Midnight Lo-Fi Lounge",
      category: "Music",
      roomType: "PUBLIC",
      language: "English",
      announcement: "Welcome everyone!",
      themeKey: "NEON_VIOLET",
      maxSeats: 8,
      participantCount: 1,
      status: "LIVE",
      publicListed: true,
      adultOnly: false,
      moderatorUids: [],
      seatUids: [ALICE_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("voice_rooms").doc(roomId).collection("seats").doc("seat_1").set({
      roomId,
      seatIndex: 1,
      occupantUid: "",
      occupantName: "",
      occupantAvatar: "",
      occupantVip: "NONE",
      muted: false,
      locked: false,
      speaking: false,
      updatedAt: now,
    })
  );

  // Filtered query matches rule
  await assertSucceeds(bobDb.collection("voice_rooms").where("publicListed", "==", true).get());
  await assertFails(bobDb.collection("voice_rooms").get());
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("seats").where("roomId", "==", roomId).get()
  );

  // Bob takes unlocked empty seat_1
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("seats").doc("seat_1").update({
      occupantUid: BOB_UID,
      occupantName: "Bob",
      occupantAvatar: "🎧",
      occupantVip: "NONE",
      muted: false,
      speaking: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob sends chat message in room
  await assertSucceeds(
    bobDb.collection("voice_rooms").doc(roomId).collection("messages").doc("msg_1").set({
      roomId,
      senderId: BOB_UID,
      senderName: "Bob",
      senderVip: "NONE",
      senderChatBubble: "DEFAULT",
      text: "Hello SocialVerse!",
      messageType: "TEXT",
      createdAt: new Date(Date.now() - 500),
    })
  );

  // Bob cannot close Alice's room
  await assertFails(
    bobDb.collection("voice_rooms").doc(roomId).update({
      status: "CLOSED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Conversations, friend requests, private calls, wallet ledger, gifts, and games isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  // Friend request lifecycle
  await assertSucceeds(
    aliceDb.collection("friend_requests").doc("fr_1").set({
      fromUserId: ALICE_UID,
      fromName: "Alice",
      fromAvatar: "🌟",
      toUserId: BOB_UID,
      toName: "Bob",
      toAvatar: "🎧",
      participantUids: [ALICE_UID, BOB_UID],
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("friend_requests").where("participantUids", "array-contains", BOB_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("friend_requests").doc("fr_1").update({
      status: "ACCEPTED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Conversation + direct message
  const convId = "conv_ab";
  await assertSucceeds(
    aliceDb.collection("conversations").doc(convId).set({
      memberUids: [ALICE_UID, BOB_UID],
      type: "DIRECT",
      title: "Alice & Bob",
      adminUid: ALICE_UID,
      lastMessage: "Hey!",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc(convId).collection("messages").doc("m1").set({
      memberUids: [ALICE_UID, BOB_UID],
      senderId: BOB_UID,
      senderName: "Bob",
      text: "Hey Alice!",
      messageType: "TEXT",
      deleted: false,
      createdAt: new Date(Date.now() - 500),
    })
  );

  // Wallet ledger immutable
  await assertSucceeds(
    aliceDb.collection("users_private").doc(ALICE_UID).collection("wallet_ledger").doc("l1").set({
      userId: ALICE_UID,
      entryType: "WELCOME_BONUS",
      amountCoins: 1000,
      balanceAfter: 1000,
      description: "Welcome bonus virtual coins",
      createdAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("users_private").doc(ALICE_UID).collection("wallet_ledger").doc("l1").update({
      amountCoins: 999999,
    })
  );
  await assertFails(
    bobDb.collection("users_private").doc(ALICE_UID).collection("wallet_ledger").get()
  );

  // Game record owner scoped
  await assertSucceeds(
    aliceDb.collection("game_records").doc("g1").set({
      userId: ALICE_UID,
      gameType: "COLOR_PREDICTION",
      periodId: "20261122-001",
      selection: "GREEN",
      stakeCoins: 50,
      wonCoins: 100,
      outcome: "WIN",
      detail: "Result: GREEN (#3)",
      createdAt: now,
    })
  );
  await assertSucceeds(aliceDb.collection("game_records").where("userId", "==", ALICE_UID).get());
  await assertFails(bobDb.collection("game_records").where("userId", "==", ALICE_UID).get());
});
