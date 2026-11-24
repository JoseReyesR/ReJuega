// Controlador de puntos de entrega
const DeliveryPointModel = require('../models/deliveryPointModel');

const DeliveryPointController = {
    getAll: async (req, res) => {
        try {
            const { activo } = req.query;
            const puntos = await DeliveryPointModel.getAll(activo !== undefined ? activo === 'true' : null);

            res.status(200).json({
                success: true,
                count: puntos.length,
                data: puntos
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener puntos de entrega',
                details: error.message
            });
        }
    },

    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const punto = await DeliveryPointModel.getById(id);

            if (!punto) {
                return res.status(404).json({
                    success: false,
                    error: 'Punto de entrega no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                data: punto
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener punto de entrega',
                details: error.message
            });
        }
    },

    create: async (req, res) => {
        try {
            const punto = req.body;

            const requiredFields = ['id', 'nombre', 'direccion', 'latitud', 'longitud'];
            const missingFields = requiredFields.filter(field => !punto[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            await DeliveryPointModel.create(punto);

            res.status(201).json({
                success: true,
                message: 'Punto de entrega creado exitosamente',
                data: { id: punto.id, nombre: punto.nombre }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear punto de entrega',
                details: error.message
            });
        }
    },

    update: async (req, res) => {
        try {
            const { id } = req.params;
            const punto = req.body;

            const affected = await DeliveryPointModel.update(id, punto);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Punto de entrega no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Punto de entrega actualizado exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar punto de entrega',
                details: error.message
            });
        }
    },

    toggleActivo: async (req, res) => {
        try {
            const { id } = req.params;
            const { activo } = req.body;

            const affected = await DeliveryPointModel.toggleActivo(id, activo);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Punto de entrega no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: `Punto ${activo ? 'activado' : 'desactivado'} exitosamente`
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al cambiar estado',
                details: error.message
            });
        }
    }
};

module.exports = DeliveryPointController;
