const crypto = require("crypto");
const express = require("express");
const admin = require("firebase-admin");

const app = express();
app.use(express.json());

const RAZORPAY_KEY_ID = process.env.RAZORPAY_KEY_ID || "";
const RAZORPAY_KEY_SECRET = process.env.RAZORPAY_KEY_SECRET || "";
const FIRESTORE_DATABASE_ID =
  process.env.FIRESTORE_DATABASE_ID ||
  "ai-studio-android-almaqra-387234af-5a64-4c0a-b106-b96acdff6dec";

if (!admin.apps.length) {
  admin.initializeApp();
}
const db = admin.firestore();
db.settings({ databaseId: FIRESTORE_DATABASE_ID });

async function authenticateUser(req, res, next) {
  const authHeader = req.headers.authorization || "";
  const idToken = authHeader.startsWith("Bearer ") ? authHeader.slice(7) : "";
  if (!idToken) {
    return res.status(401).json({ error: "Missing Firebase ID token." });
  }
  try {
    req.user = await admin.auth().verifyIdToken(idToken);
    next();
  } catch (err) {
    return res.status(401).json({ error: "Invalid Firebase ID token." });
  }
}

function razorpayAuthHeader() {
  const token = Buffer.from(`${RAZORPAY_KEY_ID}:${RAZORPAY_KEY_SECRET}`).toString("base64");
  return `Basic ${token}`;
}

// 1. POST /payments/create-order
app.post("/payments/create-order", authenticateUser, async (req, res) => {
  try {
    if (!RAZORPAY_KEY_ID || !RAZORPAY_KEY_SECRET) {
      return res.status(503).json({ error: "Razorpay server credentials not configured." });
    }
    const { courseId, courseTitle, planType, invitationCode = "" } = req.body || {};
    if (!courseId || !["individual", "group"].includes(planType)) {
      return res.status(400).json({ error: "Invalid courseId or planType." });
    }

    // Check duplicate verified enrollment before creating an order
    const enrollId = `${req.user.uid}_crs_${courseId}`;
    const existingEnroll = await db.collection("student_enrollments").doc(enrollId).get();
    if (existingEnroll.exists && existingEnroll.data().paymentStatus === "verified") {
      return res.status(409).json({ error: "Course is already paid and enrolled." });
    }

    // Authoritative fee lookup on backend
    const feeDoc = await db.collection("course_fees").doc(courseId).get();
    const defaultAmount = planType === "individual" ? 1500 : 1000;
    const amountInr = feeDoc.exists
      ? Number(planType === "individual" ? feeDoc.data().individualMonthlyInr : feeDoc.data().groupMonthlyInr) || defaultAmount
      : defaultAmount;

    const rzpResp = await fetch("https://api.razorpay.com/v1/orders", {
      method: "POST",
      headers: {
        Authorization: razorpayAuthHeader(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        amount: amountInr * 100, // paise
        currency: "INR",
        receipt: `maqra_${req.user.uid.slice(0, 8)}_${Date.now()}`,
        notes: {
          userId: req.user.uid,
          courseId,
          planType,
          invitationCode,
        },
      }),
    });
    const orderData = await rzpResp.json();
    if (!rzpResp.ok) {
      return res.status(rzpResp.status).json({ error: orderData?.error?.description || "Failed to create Razorpay order." });
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    await db.collection("payments").doc(orderData.id).set({
      orderId: orderData.id,
      razorpayPaymentId: "",
      userId: req.user.uid,
      userName: req.user.name || "Student",
      userEmail: req.user.email || "",
      courseId,
      courseTitle: courseTitle || "AL MAQRA Course",
      planType,
      amountInr,
      currency: "INR",
      paymentMethod: "Razorpay Checkout",
      status: "created",
      signatureVerified: false,
      receiptNumber: "",
      invitationCode: String(invitationCode || "").slice(0, 64),
      failureReason: "",
      whatsappRedirected: false,
      createdAt: now,
      updatedAt: now,
    });

    return res.json({
      orderId: orderData.id,
      amountInr,
      amountPaise: orderData.amount,
      currency: orderData.currency,
      keyId: RAZORPAY_KEY_ID,
    });
  } catch (err) {
    console.error("create-order error:", err);
    return res.status(500).json({ error: "Internal server error." });
  }
});

// 2. POST /payments/verify
app.post("/payments/verify", authenticateUser, async (req, res) => {
  try {
    const {
      razorpay_order_id: orderId,
      razorpay_payment_id: paymentId,
      razorpay_signature: signature,
    } = req.body || {};

    if (!orderId || !paymentId || !signature) {
      return res.status(400).json({ error: "Missing Razorpay payment signature fields." });
    }

    const expected = crypto
      .createHmac("sha256", RAZORPAY_KEY_SECRET)
      .update(`${orderId}|${paymentId}`)
      .digest("hex");

    const validSignature =
      expected.length === signature.length &&
      crypto.timingSafeEqual(Buffer.from(expected), Buffer.from(signature));

    const payRef = db.collection("payments").doc(orderId);
    const paySnap = await payRef.get();
    if (!paySnap.exists || paySnap.data().userId !== req.user.uid) {
      return res.status(404).json({ error: "Payment order not found for user." });
    }

    if (!validSignature) {
      await payRef.update({
        status: "failed",
        signatureVerified: false,
        failureReason: "Invalid payment signature",
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
      return res.status(400).json({ verified: false, error: "Signature verification failed." });
    }

    const receiptNumber = `ALM-${new Date().getFullYear()}-${paymentId.slice(-6).toUpperCase()}`;
    await payRef.update({
      razorpayPaymentId: paymentId,
      status: "verified",
      signatureVerified: true,
      receiptNumber,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return res.json({
      verified: true,
      orderId,
      razorpayPaymentId: paymentId,
      receiptNumber,
    });
  } catch (err) {
    console.error("verify error:", err);
    return res.status(500).json({ error: "Verification error." });
  }
});

// 3. POST /payments/refund (Primary Admin only)
app.post("/payments/refund", authenticateUser, async (req, res) => {
  try {
    if ((req.user.email || "").toLowerCase() !== "muhammadkerala2165@gmail.com") {
      return res.status(403).json({ error: "Admin permission required." });
    }
    const { orderId, razorpayPaymentId, reason = "Admin initiated refund" } = req.body || {};
    if (!orderId || !razorpayPaymentId) {
      return res.status(400).json({ error: "orderId and razorpayPaymentId required." });
    }
    const rzpResp = await fetch(`https://api.razorpay.com/v1/payments/${razorpayPaymentId}/refund`, {
      method: "POST",
      headers: {
        Authorization: razorpayAuthHeader(),
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ notes: { reason } }),
    });
    const refundData = await rzpResp.json();
    if (!rzpResp.ok) {
      return res.status(rzpResp.status).json({ error: refundData?.error?.description || "Refund failed." });
    }

    await db.collection("payments").doc(orderId).update({
      status: "refunded",
      failureReason: reason,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return res.json({ refunded: true, refundId: refundData.id });
  } catch (err) {
    console.error("refund error:", err);
    return res.status(500).json({ error: "Refund server error." });
  }
});

const port = process.env.PORT || 8080;
app.listen(port, () => console.log(`AL MAQRA payment backend listening on ${port}`));
