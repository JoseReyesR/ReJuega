// server.js - Servidor Express completo con todos los endpoints
require('dotenv').config();
const express = require('express');
const cors = require('cors');

// Importar todas las rutas
const materialRoutes = require('./src/routes/materialRoutes');
const campaignRoutes = require('./src/routes/campaignRoutes');
const donationRoutes = require('./src/routes/donationRoutes');
const userRoutes = require('./src/routes/userRoutes');
const deliveryPointRoutes = require('./src/routes/deliveryPointRoutes');
const pickupEventRoutes = require('./src/routes/pickupEventRoutes');
const campaignGalleryRoutes = require('./src/routes/campaignGalleryRoutes');
const notificationRoutes = require('./src/routes/notificationRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors({
    origin: process.env.ALLOWED_ORIGINS?.split(',') || '*',
    credentials: true
}));
app.use(express.json());

// Ruta raíz
app.get('/', (req, res) => {
    res.status(200).json({
        app: 'ReJuega API',
        version: '1.0.0',
        status: 'OK',
        timestamp: new Date().toISOString(),
        endpoints: {
            materiales: '/api/materiales',
            campañas: '/api/campaigns',
            donaciones: '/api/donations',
            usuarios: '/api/users',
            puntos_entrega: '/api/delivery-points',
            eventos_recojo: '/api/pickup-events',
            galeria: '/api/campaign-gallery',
            notificaciones: '/api/notifications'
        }
    });
});

// Registrar todas las rutas
app.use('/api/materiales', materialRoutes);
app.use('/api/campaigns', campaignRoutes);
app.use('/api/donations', donationRoutes);
app.use('/api/users', userRoutes);
app.use('/api/delivery-points', deliveryPointRoutes);
app.use('/api/pickup-events', pickupEventRoutes);
app.use('/api/campaign-gallery', campaignGalleryRoutes);
app.use('/api/notifications', notificationRoutes);

// Manejo de rutas no encontradas (404)
app.use((req, res) => {
    res.status(404).json({
        success: false,
        error: 'Ruta no encontrada',
        path: req.originalUrl,
        method: req.method
    });
});

// Manejo de errores global
app.use((err, req, res, next) => {
    console.error('Error:', err.message);
    res.status(500).json({
        success: false,
        error: 'Error interno del servidor',
        details: err.message
    });
});

// Iniciar servidor
app.listen(PORT, () => {
    console.log(`🚀 Servidor corriendo en http://localhost:${PORT}`);
    console.log(`📡 Modo: ${process.env.NODE_ENV || 'development'}`);
    console.log(`📋 Endpoints disponibles:`);
    console.log(`   - GET/POST /api/materiales`);
    console.log(`   - GET/POST /api/campaigns`);
    console.log(`   - GET/POST /api/donations`);
    console.log(`   - GET/POST /api/users`);
    console.log(`   - GET/POST /api/delivery-points`);
    console.log(`   - GET/POST /api/pickup-events`);
    console.log(`   - GET/POST /api/campaign-gallery`);
    console.log(`   - GET/POST /api/notifications`);
});
