// db.js - Pool de conexiones a MySQL
require('dotenv').config();
const mysql = require('mysql2/promise');

// (modificado) decimalNumbers: los DECIMAL llegan como número, no como string.
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

module.exports = pool;