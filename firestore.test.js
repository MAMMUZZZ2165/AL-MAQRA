const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfile(uid, ts) {
  return {
    userId: uid,
    displayName: "Muhammad Saleh",
    arabicName: "محمد - صالح",
    email: "muhammad@example.com",
    phone: "+918547182165",
    country: "India",
    jobTitle: "student",
    dateOfBirth: "5 April 2004",
    gender: "Male",
    preferredLanguage: "en",
    themeMode: "system",
    readingHours: 144.0,
    avgRating: 4.8,
    oneToOneCount: 6,
    attendedSessionsCount: 36,
    testsCompletedCount: 0,
    memorizedPages: 64,
    memorizedSurahs: 38,
    memorizedJuz: 3,
    memorizedAyahs: 820,
    memorizedLetters: 34200,
    gainedDeeds: 342000,
    createdAt: ts,
    updatedAt: ts,
  };
}

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

test("Unauthenticated user cannot read or write user profiles or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const ts = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, ts))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
});

test("Authenticated owner can create and partially update profile, and cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfile(ALICE_UID, created))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      preferredLanguage: "ar",
      themeMode: "dark",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      country: "Egypt",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Subcollections: Juz progress, enrollments, notes, study plans, tests, messages, session logs, feedback", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("juz_progress").doc("juz_1").set({
      juzNumber: 1,
      status: "in_progress",
      surahSummary: "Al-Fatihah - Al-Baqarah 141",
      completedAyahs: 80,
      totalAyahs: 148,
      teacherRating: 4.5,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("juz_progress").doc("juz_1").update({
      status: "completed",
      completedAyahs: 148,
      teacherRating: 5.0,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("juz_progress").doc("juz_31").set({
      juzNumber: 31,
      status: "completed",
      surahSummary: "Invalid",
      completedAyahs: 1,
      totalAyahs: 1,
      teacherRating: 5.0,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("enrollments").doc("grp_1").set({
      targetId: "grp_1",
      targetType: "group",
      title: "Al-Quran Al-Azim",
      arabicTitle: "القرآن العظيم",
      track: "Memorize the Holy Quran",
      mode: "Online",
      level: "Beginner",
      language: "Urdu",
      teacherName: "Ibrahim Rahmani",
      teacherCountry: "Saudi Arabia",
      completedSessions: 4,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").doc("note_1").set({
      title: "Madd Lazim reminder",
      surahRef: "Al-Baqarah",
      ayahRange: "1-5",
      category: "tajweed",
      content: "Elongate 6 harakat on Alif Lam Mim.",
      teacherName: "Sheikh Ali Habibullah",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_plans").doc("plan_1").set({
      title: "Fajr Hifz Circle",
      dailyTargetPages: 2,
      scheduleDays: "Sun, Tue, Thu",
      preferredTime: "5:30 AM",
      status: "active",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tests").doc("test_1").set({
      title: "Juz 30 Oral Evaluation",
      juzRange: "Juz 30",
      sheikhName: "Sheikh Muhammad Al-Barkati",
      scheduledDate: "Friday 7:00 PM",
      status: "scheduled",
      score: 0,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tests").doc("test_1").update({
      status: "completed",
      score: 96,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("tests").doc("test_1").update({
      status: "scheduled",
      score: 100,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
});

test("Admin Panel: bootstrapped admin and active managers can manage courses/teachers; non-admins are read-only", async () => {
  const adminUid = "admin_uid_1";
  const managerUid = "manager_uid_2";
  const studentUid = "student_uid_3";

  const adminDb = testEnv
    .authenticatedContext(adminUid, {
      email: "muhammadkerala2165@gmail.com",
      email_verified: true,
    })
    .firestore();
  const managerDb = testEnv
    .authenticatedContext(managerUid, {
      email: "supervisor@almaqra.org",
      email_verified: true,
    })
    .firestore();
  const studentDb = testEnv
    .authenticatedContext(studentUid, {
      email: "student@example.com",
      email_verified: true,
    })
    .firestore();

  const ts = new Date(Date.now() - 2000);

  await assertFails(
    studentDb.collection("admin_managers").doc("other@example.com").set({
      name: "Fake",
      email: "other@example.com",
      phone: "",
      role: "Academic Manager",
      status: "active",
      createdBy: studentUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    adminDb.collection("admin_managers").doc("supervisor@almaqra.org").set({
      name: "Ustadh Zaid",
      email: "supervisor@almaqra.org",
      phone: "+919497577616",
      role: "Academic Manager",
      status: "active",
      createdBy: adminUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    managerDb.collection("admin_courses").doc("crs_admin_1").set({
      title: "Intensive Tajweed Bootcamp",
      arabicTitle: "مكثفة التجويد المتقن",
      track: "Recitation & Tajweed",
      mode: "Online",
      level: "Intermediate",
      language: "English",
      teacherName: "Sheikh Tariq",
      country: "India",
      scheduleSummary: "Mon-Thu • 8:00 PM",
      seatsLeft: 25,
      visibility: "public",
      createdBy: managerUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    adminDb.collection("login_events").get()
  );
  await assertFails(studentDb.collection("login_events").get());
});

test("Invitation System: admin & student invites, group sharing guard, expiry/maxUses enforcement, and single redemption", async () => {
  const adminUid = "admin_uid_1";
  const studentUid = "student_a";
  const friendUid = "student_b";

  const adminDb = testEnv
    .authenticatedContext(adminUid, {
      email: "muhammadkerala2165@gmail.com",
      email_verified: true,
    })
    .firestore();
  const studentDb = testEnv
    .authenticatedContext(studentUid, {
      email: "studenta@example.com",
      email_verified: true,
    })
    .firestore();
  const friendDb = testEnv
    .authenticatedContext(friendUid, {
      email: "friend@example.com",
      email_verified: true,
    })
    .firestore();

  const ts = new Date(Date.now() - 2000);
  const futureExpiry = Date.now() + 3600000;

  // Admin creates a restricted group where student sharing is disabled
  await assertSucceeds(
    adminDb.collection("admin_groups").doc("grp_private_1").set({
      arabicTitle: "حلقة خاصة",
      englishTitle: "Private Hifz Circle",
      subject: "Quran",
      track: "Memorize the Holy Quran",
      teacherName: "Sheikh Ali",
      teacherEmail: "sheikh.ali@almaqra.org",
      country: "India",
      currentMembers: 5,
      maxMembers: 20,
      level: "Intermediate",
      language: "Arabic",
      allowStudentInvite: false,
      visibility: "public",
      createdBy: adminUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Student CANNOT create a group invite when allowStudentInvite == false
  await assertFails(
    studentDb.collection("invitations").doc("INV-PRIV1").set({
      code: "INV-PRIV1",
      creatorId: studentUid,
      creatorName: "Student A",
      creatorEmail: "studenta@example.com",
      creatorRole: "student",
      targetType: "group",
      targetId: "grp_private_1",
      targetTitle: "Private Hifz Circle",
      maxUses: 5,
      usedCount: 0,
      expiresAtEpochMs: futureExpiry,
      status: "active",
      sharePermitted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Student creates a valid course invite (maxUses = 1)
  await assertSucceeds(
    studentDb.collection("invitations").doc("MAQRA-CRS1").set({
      code: "MAQRA-CRS1",
      creatorId: studentUid,
      creatorName: "Student A",
      creatorEmail: "studenta@example.com",
      creatorRole: "student",
      targetType: "course",
      targetId: "crs_tajweed_basics",
      targetTitle: "Tajweed Basics",
      maxUses: 1,
      usedCount: 0,
      expiresAtEpochMs: futureExpiry,
      status: "active",
      sharePermitted: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Friend redeems invite & records attribution
  await assertSucceeds(
    friendDb.collection("invitations").doc("MAQRA-CRS1").update({
      usedCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertSucceeds(
    friendDb.collection("invitation_redemptions").doc(friendUid).set({
      inviteId: "MAQRA-CRS1",
      inviteCode: "MAQRA-CRS1",
      creatorId: studentUid,
      targetType: "course",
      targetId: "crs_tajweed_basics",
      targetTitle: "Tajweed Basics",
      userId: friendUid,
      userName: "Friend B",
      userEmail: "friend@example.com",
      redeemedAt: ts,
    })
  );

  // Exceeding maxUses (1 -> 2) fails
  await assertFails(
    friendDb.collection("invitations").doc("MAQRA-CRS1").update({
      usedCount: 2,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Student creator can list their own created invites & tracked redemptions; unfiltered list is rejected
  await assertSucceeds(
    studentDb.collection("invitations").where("creatorId", "==", studentUid).get()
  );
  await assertSucceeds(
    studentDb.collection("invitation_redemptions").where("creatorId", "==", studentUid).get()
  );
  await assertFails(studentDb.collection("invitations").get());
  await assertSucceeds(adminDb.collection("invitations").get());
});

test("Payments, Course Fees & Enrollments: zero-trust payment state transitions and cross-user isolation", async () => {
  const adminUid = "admin_uid_1";
  const studentUid = "student_pay_1";
  const otherUid = "student_other_2";

  const adminDb = testEnv
    .authenticatedContext(adminUid, {
      email: "muhammadkerala2165@gmail.com",
      email_verified: true,
    })
    .firestore();
  const studentDb = testEnv
    .authenticatedContext(studentUid, {
      email: "payer@example.com",
      email_verified: true,
    })
    .firestore();
  const otherDb = testEnv
    .authenticatedContext(otherUid, {
      email: "other@example.com",
      email_verified: true,
    })
    .firestore();

  const ts = new Date(Date.now() - 2000);

  // Only admin can configure course fees
  await assertFails(
    studentDb.collection("course_fees").doc("crs_tajweed_basics").set({
      courseId: "crs_tajweed_basics",
      courseTitle: "Tajweed Basics",
      individualMonthlyInr: 1,
      groupMonthlyInr: 1,
      trialEnabled: true,
      currency: "INR",
      visibility: "public",
      updatedBy: studentUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    adminDb.collection("course_fees").doc("crs_tajweed_basics").set({
      courseId: "crs_tajweed_basics",
      courseTitle: "Tajweed Basics",
      individualMonthlyInr: 1500,
      groupMonthlyInr: 1000,
      trialEnabled: true,
      currency: "INR",
      visibility: "public",
      updatedBy: adminUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Student CANNOT create a pre-verified payment directly
  await assertFails(
    studentDb.collection("payments").doc("order_fake_1").set({
      orderId: "order_fake_1",
      razorpayPaymentId: "pay_123456",
      userId: studentUid,
      userName: "Payer",
      userEmail: "payer@example.com",
      courseId: "crs_tajweed_basics",
      courseTitle: "Tajweed Basics",
      planType: "group",
      amountInr: 1000,
      currency: "INR",
      paymentMethod: "UPI",
      status: "verified",
      signatureVerified: true,
      receiptNumber: "ALM-2026-FAKE",
      invitationCode: "",
      failureReason: "",
      whatsappRedirected: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Creating an unverified order works
  await assertSucceeds(
    studentDb.collection("payments").doc("order_rzp_1").set({
      orderId: "order_rzp_1",
      razorpayPaymentId: "",
      userId: studentUid,
      userName: "Payer",
      userEmail: "payer@example.com",
      courseId: "crs_tajweed_basics",
      courseTitle: "Tajweed Basics",
      planType: "group",
      amountInr: 1000,
      currency: "INR",
      paymentMethod: "Razorpay Checkout",
      status: "created",
      signatureVerified: false,
      receiptNumber: "",
      invitationCode: "",
      failureReason: "",
      whatsappRedirected: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Updating with backend-verified payment ID & receipt succeeds; altering amount fails
  await assertFails(
    studentDb.collection("payments").doc("order_rzp_1").update({
      amountInr: 10,
      updatedAt: new Date(Date.now() - 800),
    })
  );
  await assertSucceeds(
    studentDb.collection("payments").doc("order_rzp_1").update({
      razorpayPaymentId: "pay_ABC123456",
      status: "verified",
      signatureVerified: true,
      receiptNumber: "ALM-2026-123456",
      paymentMethod: "Razorpay UPI",
      whatsappRedirected: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Other user cannot read or list someone else's payment
  await assertFails(otherDb.collection("payments").doc("order_rzp_1").get());
  await assertFails(otherDb.collection("payments").get());
  await assertSucceeds(
    studentDb.collection("payments").where("userId", "==", studentUid).get()
  );

  // Admin can refund payment & list all payments
  await assertSucceeds(adminDb.collection("payments").get());
  await assertSucceeds(
    adminDb.collection("payments").doc("order_rzp_1").update({
      status: "refunded",
      failureReason: "Student requested transfer",
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

test("Live Classes & Google Meet Access Gating: only enrolled students, assigned teacher, and admin can read Meet URL", async () => {
  const adminUid = "admin_uid_1";
  const teacherUid = "teacher_uid_1";
  const enrolledUid = "enrolled_student_1";
  const outsiderUid = "outsider_student_2";

  const adminDb = testEnv
    .authenticatedContext(adminUid, {
      email: "muhammadkerala2165@gmail.com",
      email_verified: true,
    })
    .firestore();
  const teacherDb = testEnv
    .authenticatedContext(teacherUid, {
      email: "sheikh.ali@almaqra.org",
      email_verified: true,
    })
    .firestore();
  const enrolledDb = testEnv
    .authenticatedContext(enrolledUid, {
      email: "enrolled@example.com",
      email_verified: true,
    })
    .firestore();
  const outsiderDb = testEnv
    .authenticatedContext(outsiderUid, {
      email: "outsider@example.com",
      email_verified: true,
    })
    .firestore();

  const ts = new Date(Date.now() - 2000);

  await assertSucceeds(
    adminDb.collection("live_classes").doc("cls_tajweed_1").set({
      title: "Evening Tajweed Halaqah",
      courseId: "crs_tajweed_basics",
      courseTitle: "Tajweed Basics",
      groupId: "grp_tajweed_en",
      groupTitle: "Precision Tajweed & Fluency",
      teacherName: "Sheikh Ali",
      teacherEmail: "sheikh.ali@almaqra.org",
      scheduledEpochMs: Date.now() + 7200000,
      dateTimeLabel: "Nov 18 • 7:00 PM",
      durationMinutes: 60,
      description: "Makharij and Qalqalah workshop",
      meetSource: "google_calendar_api",
      calendarEventId: "evt_gcal_123",
      status: "scheduled",
      lastUpdateNote: "",
      visibility: "public",
      createdBy: adminUid,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    adminDb
      .collection("live_classes")
      .doc("cls_tajweed_1")
      .collection("private")
      .doc("access")
      .set({
        meetUrl: "https://meet.google.com/abc-defg-hij",
        updatedAt: ts,
      })
  );

  // Outsider cannot read private Meet URL
  await assertFails(
    outsiderDb
      .collection("live_classes")
      .doc("cls_tajweed_1")
      .collection("private")
      .doc("access")
      .get()
  );

  // Assigned teacher can read private Meet URL and post update note
  await assertSucceeds(
    teacherDb
      .collection("live_classes")
      .doc("cls_tajweed_1")
      .collection("private")
      .doc("access")
      .get()
  );
  await assertSucceeds(
    teacherDb.collection("live_classes").doc("cls_tajweed_1").update({
      lastUpdateNote: "Please prepare Surah Al-Mulk verses 1-10.",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Student enrolls in the course -> deterministic enrollment ID unlocks private Meet URL
  await assertSucceeds(
    enrolledDb
      .collection("student_enrollments")
      .doc(`${enrolledUid}_crs_crs_tajweed_basics`)
      .set({
        userId: enrolledUid,
        userName: "Enrolled Student",
        userEmail: "enrolled@example.com",
        targetId: "crs_tajweed_basics",
        targetType: "course",
        title: "Tajweed Basics",
        arabicTitle: "أساسيات التجويد",
        track: "Recitation & Tajweed",
        mode: "Online",
        level: "Beginner",
        language: "English",
        teacherName: "Sheikh Ali",
        teacherCountry: "India",
        feePlan: "Group Classes (₹1,000)",
        paymentId: "order_rzp_1",
        paymentStatus: "verified",
        enrollmentStatus: "active",
        assignedGroupId: "grp_tajweed_en",
        completedSessions: 0,
        createdAt: ts,
        updatedAt: ts,
      })
  );

  await assertSucceeds(
    enrolledDb
      .collection("live_classes")
      .doc("cls_tajweed_1")
      .collection("private")
      .doc("access")
      .get()
  );
});
