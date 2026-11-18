// Controlador de campañas
const CampaignModel = require('../models/campaignModel');

// (nuevo) Convierte fechas ISO 8601 (de Android) a formato MySQL DATE (YYYY-MM-DD).
// (nuevo) Tolera: "2026-11-01T05:00:00.000Z", "2026-11-01", null, undefined.
function normalizarFecha(valor) {
    if (!valor) return null;
    // (nuevo) Si ya viene en YYYY-MM-DD (10 caracteres), se deja igual.
    if (typeof valor === 'string' && valor.length === 10 && valor[4] === '-' && valor[7] === '-') {
        return valor;
    }
    // (nuevo) Si es ISO completo, se toma solo la parte de la fecha.
    try {
        return valor.substring(0, 10);
    } catch {
        return null;
    }
}

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

    // (modificado) POST tolerante con normalización de fechas.
    create: async (req, res) => {
        try {
            const b = req.body;
            const id = b.id || require('uuid').v4();
            const titulo = b.titulo;
            const descripcion = b.descripcion || '';
            const fecha_inicio = normalizarFecha(b.fecha_inicio || b.fechaInicio);
            const fecha_fin = normalizarFecha(b.fecha_fin || b.fechaFin);
            const estado = b.estado || 'Activa';
            const imagen_url = b.imagen_url || b.imagenUrl || null;
            const creado_por = b.creado_por || b.creadoPor || null;

            if (!titulo || !fecha_inicio || !fecha_fin) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: ['titulo', 'fecha_inicio', 'fecha_fin'],
                    recibido: { titulo, fecha_inicio, fecha_fin }
                });
            }

            await CampaignModel.create(id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por);

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
    },

    // (modificado) Update tolerante (acepta snake_case y camelCase) y con logs de diagnóstico.
    update: async (req, res) => {
        try {
            const { id } = req.params;
            const b = req.body;
            console.log('>>> PUT /api/campaigns/' + id);
            console.log('>>> BODY RECIBIDO:', JSON.stringify(b));

            // (nuevo) Acepta ambos formatos de nombres por compatibilidad.
            const titulo = b.titulo || b.titulo;
            const descripcion = (b.descripcion !== undefined) ? b.descripcion : '';
            // (modificado) Fechas normalizadas a YYYY-MM-DD para MySQL.
            const fecha_inicio = normalizarFecha(b.fecha_inicio || b.fechaInicio);
            const fecha_fin = normalizarFecha(b.fecha_fin || b.fechaFin);
            const estado = b.estado || 'Activa';
            const imagen_url = b.imagen_url || b.imagenUrl || null;

            if (!titulo || !fecha_inicio || !fecha_fin) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    campos_recibidos: Object.keys(b)
                });
            }

            // (nuevo) Distinguir "no existe" (404) de "sin cambios".
            const existente = await CampaignModel.getById(id);
            if (!existente) {
                return res.status(404).json({
                    success: false,
                    error: 'Campaña no encontrada en MySQL',
                    id_buscado: id
                });
            }

            await CampaignModel.update(id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url);

            console.log('>>> UPDATE OK:', id);
            res.status(200).json({ success: true, message: 'Campaña actualizada exitosamente' });
        } catch (error) {
            console.error('>>> ERROR EN PUT /api/campaigns/:id →', error.message);
            res.status(500).json({
                success: false,
                error: 'Error al actualizar campaña',
                details: error.message
            });
        }
    },

    // PUT /api/campaigns/:id/estado
    updateEstado: async (req, res) => {
        try {
            const { id } = req.params;
            const { estado } = req.body;

            if (!['Activa', 'Cerrada'].includes(estado)) {
                return res.status(400).json({ success: false, error: 'Estado no válido', validos: ['Activa', 'Cerrada'] });
            }

            const affected = await CampaignModel.updateEstado(id, estado);

            if (affected === 0) {
                return res.status(404).json({ success: false, error: 'Campaña no encontrada' });
            }

            res.status(200).json({ success: true, message: `Campaña ahora está: ${estado}` });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Error al cambiar estado', details: error.message });
        }
    }
};

module.exports = CampaignController;
