// server.js - Servidor Express principal
require('dotenv').config();
const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors({ origin: process.env.ALLOWED_ORIGINS?.split(',') || '*' }));
app.use(express.json());

// Ruta de prueba
app.get('/', (req, res) => {
    res.status(200).json({
        app: 'ReJuega API',
        version: '1.0.0',
        status: 'OK',
        timestamp: new Date().toISOString()
    });
});

// Iniciar servidor
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
    console.log(`📡 Modo: ${process.env.NODE_ENV || 'development'}`);
});