// Controlador de donaciones
const DonationModel = require('../models/donationModel');
const { v4: uuidv4 } = require('uuid');

// (nuevo) Convierte cualquier fecha de MySQL a milisegundos UTC.
// (modificado) Convierte CUALQUIER formato de fecha de MySQL a milisegundos.
function aMillis(valor) {
    if (valor === null || valor === undefined) return null;

    // (nuevo) Si ya es número, devolverlo.
    if (typeof valor === 'number') return valor;

    // (nuevo) Si es un objeto Date de JS (mysql2 sin dateStrings), usar getTime().
    if (valor instanceof Date) {
        const t = valor.getTime();
        return isNaN(t) ? null : t;
    }

    // (nuevo) Si es string "YYYY-MM-DD HH:MM:SS" o ISO, parsear.
    if (typeof valor === 'string') {
        const iso = valor.replace(' ', 'T');
        const t = new Date(iso).getTime();
        return isNaN(t) ? null : t;
    }

    return null;
}

const DonationController = {
    // GET /api/donations
    getAll: async (req, res) => {
        try {
            const { user_id, campaign_id } = req.query;
            const donations = await DonationModel.getAll(user_id, campaign_id);

            // (nuevo) Normaliza fecha_registro a milisegundos antes de enviar.
            const data = donations.map(d => ({
                ...d,
                fecha_registro: aMillis(d.fecha_registro)
            }));

            res.status(200).json({ success: true, count: data.length, data });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Error al obtener donaciones', details: error.message });
        }
    },

    // GET /api/donations/:id
    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const donation = await DonationModel.getById(id);
            if (!donation) return res.status(404).json({ success: false, error: 'Donación no encontrada' });

            const history = await DonationModel.getHistory(id);
            const data = { ...donation, fecha_registro: aMillis(donation.fecha_registro), historial: history };

            res.status(200).json({ success: true, data });
        } catch (error) {
            res.status(500).json({ success: false, error: 'Error al obtener donación', details: error.message });
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
    },

    // (nuevo) PUT /api/donations/:id  (solo mientras está Pendiente)
    update: async (req, res) => {
        try {
            const { id } = req.params;
            const b = req.body;

            const existente = await DonationModel.getById(id);
            if (!existente) return res.status(404).json({ success: false, error: 'Donación no encontrada' });

            // (nuevo) Regla de negocio: solo se edita en Pendiente.
            if (existente.estado_trazabilidad !== 'Pendiente') {
                return res.status(400).json({
                    success: false,
                    error: 'Solo puedes editar una donación en estado Pendiente'
                });
            }

            await DonationModel.update(id, b.nombre_juguete, b.descripcion, b.condicion, b.material_tipo, b.co2_ahorrado_kg);
            res.status(200).json({ success: true, message: 'Donación actualizada' });
        } catch (e) {
            res.status(500).json({ success: false, error: 'Error al actualizar', details: e.message });
        }
    },

    // (nuevo) DELETE /api/donations/:id  (solo mientras está Pendiente)
    delete: async (req, res) => {
        try {
            const { id } = req.params;
            const existente = await DonationModel.getById(id);
            if (!existente) return res.status(404).json({ success: false, error: 'Donación no encontrada' });

            if (existente.estado_trazabilidad !== 'Pendiente') {
                return res.status(400).json({ success: false, error: 'Solo puedes eliminar una donación en estado Pendiente' });
            }

            await DonationModel.delete(id);
            res.status(200).json({ success: true, message: 'Donación eliminada' });
        } catch (e) {
            res.status(500).json({ success: false, error: 'Error al eliminar', details: e.message });
        }
    }
};

module.exports = DonationController;
