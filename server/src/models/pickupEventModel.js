// Modelo de eventos de recojo
const db = require('../../db');

const PickupEventModel = {
    getAll: async (campaign_id = null, activo = null) => {
        let query = 'SELECT * FROM pickup_events';
        let params = [];
        let conditions = [];

        if (campaign_id) {
            conditions.push('campaign_id = ?');
            params.push(campaign_id);
        }
        if (activo !== null) {
            conditions.push('activo = ?');
            params.push(activo);
        }

        if (conditions.length > 0) {
            query += ' WHERE ' + conditions.join(' AND ');
        }
        query += ' ORDER BY fecha DESC';

        const [rows] = await db.execute(query, params);
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM pickup_events WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (evento) => {
        const [result] = await db.execute(
            `INSERT INTO pickup_events (id, campaign_id, nombre, lugar, direccion, fecha, hora, latitud, longitud, activo, creado_por)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [evento.id, evento.campaign_id, evento.nombre, evento.lugar, evento.direccion,
             evento.fecha, evento.hora, evento.latitud, evento.longitud, evento.activo, evento.creado_por]
        );
        return result.insertId;
    },

    update: async (id, evento) => {
        const [result] = await db.execute(
            `UPDATE pickup_events SET campaign_id = ?, nombre = ?, lugar = ?, direccion = ?,
             fecha = ?, hora = ?, latitud = ?, longitud = ?, activo = ? WHERE id = ?`,
            [evento.campaign_id, evento.nombre, evento.lugar, evento.direccion,
             evento.fecha, evento.hora, evento.latitud, evento.longitud, evento.activo, id]
        );
        return result.affectedRows;
    },

    toggleActivo: async (id, activo) => {
        const [result] = await db.execute(
            'UPDATE pickup_events SET activo = ? WHERE id = ?',
            [activo, id]
        );
        return result.affectedRows;
    }
};

module.exports = PickupEventModel;
