const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_creator_01";
const BOB_UID = "bob_creator_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

test("Unauthenticated user cannot read or write any NEXUS AI collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/projects`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/scripts`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/researchBriefs`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/calendarItems`).get());
  await assertFails(unauthDb.collection(`users/${ALICE_UID}/clipAnalyses`).get());
  await assertFails(
    unauthDb.collection(`users/${ALICE_UID}/projects`).doc("proj_1").set({
      userId: ALICE_UID,
      title: "Hack",
      niche: "Tech",
      format: "LONG_FORM",
      stage: "IDEA",
      targetPlatform: "YouTube",
      hook: "Hook",
      notes: "Notes",
      clipScore: 80,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Projects: owner create, partial update, list, and security rejections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Valid create (matches Kotlin ProjectRepository map without @DocumentId `id` or null `scheduledDate`)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      title: "AI Hardware Breakdown",
      niche: "Tech",
      format: "LONG_FORM",
      stage: "RESEARCH",
      targetPlatform: "YouTube",
      hook: "Why every creator is switching to local NPUs",
      notes: "Cover latency and power benchmarks.",
      clipScore: 88,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Valid list query
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );

  // Valid partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").update({
      stage: "SCRIPTED",
      scheduledDate: "2026-11-25",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject cross-user read and write
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject @DocumentId `id` field inside body
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_2").set({
      id: "proj_2",
      userId: ALICE_UID,
      title: "Bad ID payload",
      niche: "Tech",
      format: "SHORT",
      stage: "IDEA",
      targetPlatform: "YouTube Shorts",
      hook: "",
      notes: "",
      clipScore: 50,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject immutable createdAt mutation and invalid stage enum
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").update({
      stage: "INVALID_STAGE",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("ResearchBriefs, Scripts, CalendarItems, and ClipAnalyses: valid CRUD & bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 3000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("researchBriefs").doc("brief_1").set({
      userId: ALICE_UID,
      topic: "Agentic Coding Workflows",
      audience: "Mobile Engineers",
      summary: "High search velocity around Kotlin + AI tools.",
      viralScore: 91,
      angles: ["5 mistakes devs make", "Live benchmark comparison"],
      keywords: ["kotlin", "ai studio", "android"],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("scripts").doc("script_1").set({
      userId: ALICE_UID,
      title: "5 Kotlin Patterns for AI Apps",
      format: "SHORT",
      tone: "High-Energy Tech",
      targetDurationSec: 55,
      hook: "Stop blocking your Compose UI on LLM calls.",
      body: "[00:03] Use StateFlow + withTimeout...",
      cta: "Follow for part 2.",
      visualCues: ["00:00 Screen recording", "00:10 Code overlay"],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("calendarItems").doc("cal_1").set({
      userId: ALICE_UID,
      title: "Publish Kotlin Patterns Short",
      platform: "YouTube Shorts",
      scheduledDate: "2026-11-22",
      scheduledTime: "18:00",
      status: "PLANNED",
      notes: "Use high-contrast cyan thumbnail.",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("calendarItems").doc("cal_1").update({
      status: "READY",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("clipAnalyses").doc("clip_1").set({
      userId: ALICE_UID,
      clipTitle: "Opening 30s Hook Test",
      sourceSummary: "Fast pacing demo of NEXUS AI studio.",
      retentionScore: 86,
      hookStrength: 92,
      pacingVerdict: "Tight pacing with strong pattern interrupt at 00:04",
      highlights: ["00:00-00:04 Strong visual hook", "00:12-00:19 Live metric reveal"],
      improvements: ["Trim 1.5s pause before CTA"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject future client timestamp tampering
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("clipAnalyses").doc("clip_bad").set({
      userId: ALICE_UID,
      clipTitle: "Future Timestamp",
      sourceSummary: "Test",
      retentionScore: 50,
      hookStrength: 50,
      pacingVerdict: "OK",
      highlights: [],
      improvements: [],
      createdAt: new Date(Date.now() + 60000),
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Reject invalid calendar date format
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("calendarItems").doc("cal_bad").set({
      userId: ALICE_UID,
      title: "Bad Date",
      platform: "YouTube",
      scheduledDate: "11/22/2026",
      scheduledTime: "18:00",
      status: "PLANNED",
      notes: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user delete blocked
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("scripts").doc("script_1").delete()
  );
  // Owner delete allowed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("scripts").doc("script_1").delete()
  );
});
