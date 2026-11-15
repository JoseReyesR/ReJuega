// Controlador de donaciones
const DonationModel = require('../models/donationModel');
const { v4: uuidv4 } = require('uuid');

const DonationController = {
    // GET /api/donations
    getAll: async (req, res) => {
        try {
            const { user_id, campaign_id } = req.query;
            const donations = await DonationModel.getAll(user_id, campaign_id);

            res.status(200).json({
                success: true,
                count: donations.length,
                data: donations
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener donaciones',
                details: error.message
            });
        }
    },

    // GET /api/donations/:id
    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const donation = await DonationModel.getById(id);

            if (!donation) {
                return res.status(404).json({
                    success: false,
                    error: 'Donación no encontrada'
                });
            }

            // Obtener historial
            const history = await DonationModel.getHistory(id);

            res.status(200).json({
                success: true,
                data: { ...donation, historial: history }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener donación',
                details: error.message
            });
        }
    },

    // POST /api/donations
    create: async (req, res) => {
        try {
            const donation = req.body;

            // Validación de campos obligatorios
            const requiredFields = ['user_id', 'campaign_id', 'nombre_juguete', 'foto_url'];
            const missingFields = requiredFields.filter(field => !donation[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            // Generar ID único si no viene
            donation.id = donation.id || uuidv4();
            donation.estado_trazabilidad = donation.estado_trazabilidad || 'Pendiente';

            await DonationModel.create(donation);

            // Agregar primer registro al historial
            await DonationModel.addHistory(donation.id, 'Pendiente', donation.user_id);

            res.status(201).json({
                success: true,
                message: 'Donación registrada exitosamente',
                data: { id: donation.id, estado: 'Pendiente' }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al registrar donación',
                details: error.message
            });
        }
    },

    // PUT /api/donations/:id/estado
    updateEstado: async (req, res) => {
        try {
            const { id } = req.params;
            const { estado_trazabilidad, motivo_rechazo, cambiado_por } = req.body;

            // Validar estado
            const estadosValidos = ['Pendiente', 'En Revision', 'Aprobado', 'Entregado', 'Rechazado'];
            if (!estadosValidos.includes(estado_trazabilidad)) {
                return res.status(400).json({
                    success: false,
                    error: 'Estado no válido',
                    validos: estadosValidos
                });
            }

            const affected = await DonationModel.updateEstado(id, estado_trazabilidad, motivo_rechazo);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Donación no encontrada'
                });
            }

            // Agregar al historial
            await DonationModel.addHistory(id, estado_trazabilidad, cambiado_por);

            res.status(200).json({
                success: true,
                message: `Estado actualizado a: ${estado_trazabilidad}`,
                data: { id, estado: estado_trazabilidad }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar estado',
                details: error.message
            });
        }
    }
};

module.exports = DonationController;
