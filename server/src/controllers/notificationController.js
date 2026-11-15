// Controlador de notificaciones
const NotificationModel = require('../models/notificationModel');

const NotificationController = {
    getByUser: async (req, res) => {
        try {
            const { user_id } = req.params;
            const { leida } = req.query;

            const notificaciones = await NotificationModel.getByUser(user_id, leida !== undefined ? leida === 'true' : null);

            res.status(200).json({
                success: true,
                count: notificaciones.length,
                data: notificaciones
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener notificaciones',
                details: error.message
            });
        }
    },

    create: async (req, res) => {
        try {
            const notificacion = req.body;

            const requiredFields = ['id', 'user_id', 'titulo', 'mensaje'];
            const missingFields = requiredFields.filter(field => !notificacion[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            await NotificationModel.create(notificacion);

            res.status(201).json({
                success: true,
                message: 'Notificación creada exitosamente',
                data: { id: notificacion.id }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear notificación',
                details: error.message
            });
        }
    },

    markAsRead: async (req, res) => {
        try {
            const { id } = req.params;
            const affected = await NotificationModel.markAsRead(id);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Notificación no encontrada'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Notificación marcada como leída'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al marcar notificación',
                details: error.message
            });
        }
    },

    markAllAsRead: async (req, res) => {
        try {
            const { user_id } = req.params;
            const affected = await NotificationModel.markAllAsRead(user_id);

            res.status(200).json({
                success: true,
                message: `${affected} notificaciones marcadas como leídas`
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al marcar notificaciones',
                details: error.message
            });
        }
    },

    countUnread: async (req, res) => {
        try {
            const { user_id } = req.params;
            const count = await NotificationModel.countUnread(user_id);

            res.status(200).json({
                success: true,
                data: { count }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al contar notificaciones',
                details: error.message
            });
        }
    }
};

module.exports = NotificationController;
