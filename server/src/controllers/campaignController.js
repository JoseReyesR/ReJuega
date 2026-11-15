// Controlador de campañas
const CampaignModel = require('../models/campaignModel');

const CampaignController = {
    // GET /api/campaigns
    getAll: async (req, res) => {
        try {
            const { estado } = req.query;
            const campaigns = await CampaignModel.getAll(estado);

            res.status(200).json({
                success: true,
                count: campaigns.length,
                data: campaigns
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener campañas',
                details: error.message
            });
        }
    },

    // GET /api/campaigns/:id
    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const campaign = await CampaignModel.getById(id);

            if (!campaign) {
                return res.status(404).json({
                    success: false,
                    error: 'Campaña no encontrada'
                });
            }

            res.status(200).json({
                success: true,
                data: campaign
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener campaña',
                details: error.message
            });
        }
    },

    // POST /api/campaigns
    create: async (req, res) => {
        try {
            const { id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por } = req.body;

            // Validación
            if (!titulo || !fecha_inicio || !fecha_fin) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: ['titulo', 'fecha_inicio', 'fecha_fin']
                });
            }

            await CampaignModel.create(id, titulo, descripcion, fecha_inicio, fecha_fin, estado || 'Activa', imagen_url, creado_por);

            res.status(201).json({
                success: true,
                message: 'Campaña creada exitosamente',
                data: { id, titulo }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear campaña',
                details: error.message
            });
        }
    }
};

module.exports = CampaignController;
