// db.js - Pool de conexiones a MySQL y Firebase Admin
require('dotenv').config();
const mysql = require('mysql2/promise');
const { initializeApp, cert, getApps } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const serviceAccount = require('./firebase-service-account.json');

// Pool de MySQL.
const pool = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    port: parseInt(process.env.DB_PORT) || 3306,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0,
    decimalNumbers: true
});

// Prueba de conexión al iniciar
pool.getConnection()
    .then(conn => {
        console.log('✅ Conectado a MySQL:', process.env.DB_NAME);
        conn.release();
    })
    .catch(err => {
        console.error('❌ Error conectando a MySQL:', err.message);
    });

// (nuevo) Firebase Admin para escribir en Firestore desde el servidor.
if (!getApps().length) {
    initializeApp({
        credential: cert(serviceAccount)
    });
}
const firestore = getFirestore();

module.exports = { pool, execute: pool.execute.bind(pool), firestore };
