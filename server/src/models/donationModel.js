// Modelo de donaciones
const db = require('../../db');

const DonationModel = {
    getAll: async (user_id = null, campaign_id = null) => {
        let query = 'SELECT * FROM donations';
        let params = [];
        let conditions = [];

        if (user_id) {
            conditions.push('user_id = ?');
            params.push(user_id);
        }
        if (campaign_id) {
            conditions.push('campaign_id = ?');
            params.push(campaign_id);
        }

        if (conditions.length > 0) {
            query += ' WHERE ' + conditions.join(' AND ');
        }
        query += ' ORDER BY fecha_registro DESC';

        const [rows] = await db.execute(query, params);
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM donations WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (donation) => {
        const [result] = await db.execute(
            `INSERT INTO donations (id, user_id, campaign_id, nombre_juguete, descripcion, condicion,
             foto_url, estado_trazabilidad, material_tipo, co2_ahorrado_kg)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                donation.id, donation.user_id, donation.campaign_id,
                donation.nombre_juguete, donation.descripcion, donation.condicion,
                donation.foto_url, donation.estado_trazabilidad || 'Pendiente',
                donation.material_tipo, donation.co2_ahorrado_kg || 0
            ]
        );
        return result.insertId;
    },

    updateEstado: async (id, estado_trazabilidad, motivo_rechazo = null) => {
        const [result] = await db.execute(
            'UPDATE donations SET estado_trazabilidad = ?, motivo_rechazo = ? WHERE id = ?',
            [estado_trazabilidad, motivo_rechazo, id]
        );
        return result.affectedRows;
    },

    // Agregar entrada al historial
    addHistory: async (donation_id, estado, cambiado_por) => {
        const [result] = await db.execute(
            'INSERT INTO donation_history (donation_id, estado, cambiado_por) VALUES (?, ?, ?)',
            [donation_id, estado, cambiado_por]
        );
        return result.insertId;
    },

    getHistory: async (donation_id) => {
        const [rows] = await db.execute(
            'SELECT * FROM donation_history WHERE donation_id = ? ORDER BY fecha_cambio ASC',
            [donation_id]
        );
        return rows;
    },

    update: async (id, nombre_juguete, descripcion, condicion, material_tipo, co2_ahorrado_kg) => {
        const [result] = await db.execute(
            'UPDATE donations SET nombre_juguete=?, descripcion=?, condicion=?, material_tipo=?, co2_ahorrado_kg=? WHERE id=?',
            [nombre_juguete, descripcion, condicion, material_tipo, co2_ahorrado_kg, id]
        );
        return result.affectedRows;
    },

    delete: async (id) => {
        const [result] = await db.execute('DELETE FROM donations WHERE id = ?', [id]);
        return result.affectedRows;
    }
};

module.exports = DonationModel;
