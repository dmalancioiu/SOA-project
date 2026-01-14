const express = require('express');
const app = express();
const morgan = require('morgan');
const { v4: uuidv4 } = require('uuid');
const mysql = require('mysql2/promise');
const nodemailer = require('nodemailer');

// Middleware
app.use(express.json());
app.use(morgan('combined'));

// Logger
const logger = {
  info: (msg) => console.log(`[INFO] ${new Date().toISOString()} - ${msg}`),
  error: (msg) => console.error(`[ERROR] ${new Date().toISOString()} - ${msg}`),
  warn: (msg) => console.warn(`[WARN] ${new Date().toISOString()} - ${msg}`),
  debug: (msg) => process.env.DEBUG && console.log(`[DEBUG] ${new Date().toISOString()} - ${msg}`)
};

// Database configuration
const db_config = {
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'food_delivery',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
};

// Email configuration (simulated)
const email_config = {
  from: process.env.EMAIL_FROM || 'noreply@fooddelivery.com',
  smtpHost: process.env.SMTP_HOST,
  smtpPort: process.env.SMTP_PORT || 587,
  smtpUser: process.env.SMTP_USER,
  smtpPassword: process.env.SMTP_PASSWORD,
  useSimulation: process.env.EMAIL_SIMULATION !== 'false'
};

// Email transporter setup
let emailTransporter = null;
if (!email_config.useSimulation && email_config.smtpHost) {
  try {
    emailTransporter = nodemailer.createTransport({
      host: email_config.smtpHost,
      port: email_config.smtpPort,
      secure: email_config.smtpPort === 465,
      auth: {
        user: email_config.smtpUser,
        pass: email_config.smtpPassword
      }
    });
    logger.info('Email transporter initialized with SMTP');
  } catch (e) {
    logger.error(`Failed to initialize email transporter: ${e.message}`);
  }
}

// Database pool
let pool = null;
try {
  pool = mysql.createPool(db_config);
  logger.info(`Database pool created for ${db_config.host}`);
} catch (e) {
  logger.error(`Failed to create database pool: ${e.message}`);
}

// Health check endpoint
app.get('/health', async (req, res) => {
  try {
    let dbHealthy = false;
    if (pool) {
      try {
        const conn = await pool.getConnection();
        await conn.ping();
        conn.release();
        dbHealthy = true;
      } catch (e) {
        logger.warn(`Database health check failed: ${e.message}`);
      }
    }

    return res.status(dbHealthy ? 200 : 503).json({
      status: dbHealthy ? 'healthy' : 'degraded',
      timestamp: new Date().toISOString(),
      database: dbHealthy ? 'connected' : 'disconnected',
      emailer: emailTransporter ? 'configured' : 'simulated'
    });
  } catch (e) {
    logger.error(`Health check error: ${e.message}`);
    return res.status(503).json({
      status: 'unhealthy',
      error: e.message
    });
  }
});

// Main handler - Order completion
app.post('/', async (req, res) => {
  const requestId = uuidv4();
  try {
    logger.info(`[${requestId}] Processing order completion request`);

    if (!req.body || typeof req.body !== 'object') {
      logger.error(`[${requestId}] Invalid request body`);
      return res.status(400).json({
        success: false,
        error: 'Invalid request body'
      });
    }

    const {
      orderId,
      customerId,
      orderTotal,
      deliveryTime,
      customerEmail,
      customerName
    } = req.body;

    // Validate required fields
    const requiredFields = ['orderId', 'customerId', 'orderTotal'];
    const missingFields = requiredFields.filter(field => !(field in req.body));

    if (missingFields.length > 0) {
      logger.error(`[${requestId}] Missing required fields: ${missingFields.join(', ')}`);
      return res.status(400).json({
        success: false,
        error: `Missing required fields: ${missingFields.join(', ')}`
      });
    }

    logger.info(`[${requestId}] Order ${orderId} from customer ${customerId} ready for completion`);

    // Step 1: Send thank you email
    const emailSent = await sendThankYouEmail(
      requestId,
      customerId,
      customerEmail,
      customerName,
      orderId,
      orderTotal
    );

    // Step 2: Update customer loyalty points
    const loyaltyUpdated = await updateLoyaltyPoints(
      requestId,
      customerId,
      orderTotal
    );

    // Step 3: Generate order receipt
    const receipt = await generateReceipt(
      requestId,
      orderId,
      customerId,
      orderTotal,
      deliveryTime
    );

    // Step 4: Store receipt in database
    const receiptStored = await storeReceiptInDatabase(
      requestId,
      receipt
    );

    const result = {
      success: emailSent && loyaltyUpdated && receiptStored,
      orderId,
      customerId,
      receiptId: receipt.receiptId,
      email: {
        sent: emailSent,
        address: customerEmail || 'not provided'
      },
      loyalty: {
        updated: loyaltyUpdated,
        pointsAwarded: Math.floor(orderTotal * 10)
      },
      receipt: {
        stored: receiptStored,
        receiptId: receipt.receiptId,
        generatedAt: receipt.generatedAt
      }
    };

    logger.info(`[${requestId}] Order completion processed successfully`);
    return res.status(200).json({
      success: result.success,
      data: result,
      message: 'Order completion processed successfully'
    });

  } catch (error) {
    logger.error(`[${requestId}] Error processing order completion: ${error.message}`, error);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Helper function: Send thank you email
async function sendThankYouEmail(requestId, customerId, email, customerName, orderId, orderTotal) {
  try {
    logger.info(`[${requestId}] Sending thank you email to customer ${customerId}`);

    if (email_config.useSimulation || !emailTransporter) {
      // Simulated email send
      logger.info(`[${requestId}] [SIMULATED EMAIL] Thank you email sent to ${email}`);
      logger.info(`[${requestId}] Subject: Order Complete - Thank You!`);
      logger.info(`[${requestId}] Body: Thank you ${customerName} for order ${orderId}. Total: $${orderTotal}`);
      return true;
    }

    // Real email send
    const mailOptions = {
      from: email_config.from,
      to: email,
      subject: 'Order Complete - Thank You!',
      html: `
        <h2>Thank you for your order!</h2>
        <p>Dear ${customerName},</p>
        <p>Your order <strong>#${orderId}</strong> has been completed.</p>
        <p>Order Total: <strong>$${orderTotal.toFixed(2)}</strong></p>
        <p>Thank you for choosing our food delivery service!</p>
        <p>Best regards,<br>Food Delivery Team</p>
      `
    };

    const info = await emailTransporter.sendMail(mailOptions);
    logger.info(`[${requestId}] Email sent successfully. Message ID: ${info.messageId}`);
    return true;

  } catch (error) {
    logger.error(`[${requestId}] Failed to send email: ${error.message}`);
    return false;
  }
}

// Helper function: Update loyalty points
async function updateLoyaltyPoints(requestId, customerId, orderTotal) {
  try {
    logger.info(`[${requestId}] Updating loyalty points for customer ${customerId}`);

    if (!pool) {
      logger.warn(`[${requestId}] Database pool not available, skipping loyalty update`);
      return false;
    }

    const pointsToAward = Math.floor(orderTotal * 10);

    const query = `
      UPDATE customers
      SET loyalty_points = loyalty_points + ?
      WHERE customer_id = ?
    `;

    const conn = await pool.getConnection();
    try {
      const result = await conn.query(query, [pointsToAward, customerId]);
      logger.info(`[${requestId}] Loyalty points updated for customer ${customerId}: +${pointsToAward} points`);
      return result[0].affectedRows > 0;
    } finally {
      conn.release();
    }

  } catch (error) {
    logger.error(`[${requestId}] Error updating loyalty points: ${error.message}`);
    return false;
  }
}

// Helper function: Generate receipt
async function generateReceipt(requestId, orderId, customerId, orderTotal, deliveryTime) {
  try {
    logger.info(`[${requestId}] Generating receipt for order ${orderId}`);

    const receipt = {
      receiptId: `RECEIPT-${uuidv4()}`,
      orderId,
      customerId,
      orderTotal,
      deliveryTime: deliveryTime || null,
      generatedAt: new Date().toISOString(),
      status: 'completed',
      itemsCount: 1,
      taxAmount: Number((orderTotal * 0.1).toFixed(2)),
      deliveryFee: Number((orderTotal * 0.05).toFixed(2))
    };

    logger.debug(`[${requestId}] Receipt generated: ${JSON.stringify(receipt)}`);
    return receipt;

  } catch (error) {
    logger.error(`[${requestId}] Error generating receipt: ${error.message}`);
    throw error;
  }
}

// Helper function: Store receipt in database
async function storeReceiptInDatabase(requestId, receipt) {
  try {
    logger.info(`[${requestId}] Storing receipt in database`);

    if (!pool) {
      logger.warn(`[${requestId}] Database pool not available, skipping receipt storage`);
      return false;
    }

    const query = `
      INSERT INTO receipts (
        receipt_id, order_id, customer_id, order_total,
        tax_amount, delivery_fee, delivery_time,
        status, created_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `;

    const conn = await pool.getConnection();
    try {
      const result = await conn.query(query, [
        receipt.receiptId,
        receipt.orderId,
        receipt.customerId,
        receipt.orderTotal,
        receipt.taxAmount,
        receipt.deliveryFee,
        receipt.deliveryTime,
        receipt.status,
        new Date()
      ]);

      logger.info(`[${requestId}] Receipt stored in database: ${receipt.receiptId}`);
      return result[0].affectedRows > 0;
    } finally {
      conn.release();
    }

  } catch (error) {
    logger.error(`[${requestId}] Error storing receipt in database: ${error.message}`);
    return false;
  }
}

// Get receipt endpoint
app.get('/receipt/:receiptId', async (req, res) => {
  try {
    const { receiptId } = req.params;
    logger.info(`Retrieving receipt: ${receiptId}`);

    if (!pool) {
      return res.status(503).json({
        success: false,
        error: 'Database not available'
      });
    }

    const query = 'SELECT * FROM receipts WHERE receipt_id = ?';
    const conn = await pool.getConnection();
    try {
      const [rows] = await conn.query(query, [receiptId]);
      conn.release();

      if (rows.length === 0) {
        return res.status(404).json({
          success: false,
          error: 'Receipt not found'
        });
      }

      return res.status(200).json({
        success: true,
        data: rows[0]
      });
    } finally {
      conn.release();
    }

  } catch (error) {
    logger.error(`Error retrieving receipt: ${error.message}`);
    return res.status(500).json({
      success: false,
      error: error.message
    });
  }
});

// Start server
const PORT = process.env.PORT || 8080;
app.listen(PORT, '0.0.0.0', () => {
  logger.info(`Order completion function listening on port ${PORT}`);
});

module.exports = app;
