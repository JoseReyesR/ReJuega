// Controlador de eventos de recojo
const PickupEventModel = require('../models/pickupEventModel');

const PickupEventController = {
    getAll: async (req, res) => {
        try {
            const { campaign_id, activo } = req.query;
            const eventos = await PickupEventModel.getAll(campaign_id, activo !== undefined ? activo === 'true' : null);

            res.status(200).json({
                success: true,
                count: eventos.length,
                data: eventos
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener eventos de recojo',
                details: error.message
            });
        }
    },

    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const evento = await PickupEventModel.getById(id);

            if (!evento) {
                return res.status(404).json({
                    success: false,
                    error: 'Evento de recojo no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                data: evento
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener evento de recojo',
                details: error.message
            });
        }
    },

    create: async (req, res) => {
        try {
            const evento = req.body;

            const requiredFields = ['id', 'campaign_id', 'nombre', 'fecha', 'hora', 'latitud', 'longitud'];
            const missingFields = requiredFields.filter(field => !evento[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            await PickupEventModel.create(evento);

            res.status(201).json({
                success: true,
                message: 'Evento de recojo creado exitosamente',
                data: { id: evento.id, nombre: evento.nombre }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear evento de recojo',
                details: error.message
            });
        }
    },

    update: async (req, res) => {
        try {
            const { id } = req.params;
            const evento = req.body;

            const affected = await PickupEventModel.update(id, evento);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Evento de recojo no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Evento de recojo actualizado exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar evento de recojo',
                details: error.message
            });
        }
    },

    toggleActivo: async (req, res) => {
        try {
            const { id } = req.params;
            const { activo } = req.body;

            const affected = await PickupEventModel.toggleActivo(id, activo);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Evento de recojo no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: `Evento ${activo ? 'activado' : 'desactivado'} exitosamente`
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

module.exports = PickupEventController;
