// Controlador de galería de campañas
const CampaignGalleryModel = require('../models/campaignGalleryModel');

const CampaignGalleryController = {
    getByCampaign: async (req, res) => {
        try {
            const { campaign_id } = req.params;
            const evidencias = await CampaignGalleryModel.getByCampaign(campaign_id);

            res.status(200).json({
                success: true,
                count: evidencias.length,
                data: evidencias
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener evidencias',
                details: error.message
            });
        }
    },

    create: async (req, res) => {
        try {
            const evidencia = req.body;

            const requiredFields = ['id', 'campaign_id', 'foto_url'];
            const missingFields = requiredFields.filter(field => !evidencia[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            await CampaignGalleryModel.create(evidencia);

            res.status(201).json({
                success: true,
                message: 'Evidencia agregada exitosamente',
                data: { id: evidencia.id }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al agregar evidencia',
                details: error.message
            });
        }
    },

    delete: async (req, res) => {
        try {
            const { id } = req.params;
            const affected = await CampaignGalleryModel.delete(id);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Evidencia no encontrada'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Evidencia eliminada exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al eliminar evidencia',
                details: error.message
            });
        }
    }
};

module.exports = CampaignGalleryController;
