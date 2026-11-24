// Modelo de puntos de entrega
const db = require('../../db');

const DeliveryPointModel = {
    getAll: async (activo = null) => {
        let query = 'SELECT * FROM delivery_points';
        let params = [];

        if (activo !== null) {
            query += ' WHERE activo = ?';
            params.push(activo);
        }

        query += ' ORDER BY nombre';
        const [rows] = await db.execute(query, params);
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM delivery_points WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (punto) => {
        const [result] = await db.execute(
            `INSERT INTO delivery_points (id, nombre, direccion, horario, dias_atencion, hora_inicio, hora_fin, latitud, longitud, activo, creado_por)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [punto.id, punto.nombre, punto.direccion, punto.horario, punto.dias_atencion,
             punto.hora_inicio, punto.hora_fin, punto.latitud, punto.longitud, punto.activo, punto.creado_por]
        );
        return result.insertId;
    },

    update: async (id, punto) => {
        const [result] = await db.execute(
            `UPDATE delivery_points SET nombre = ?, direccion = ?, horario = ?, dias_atencion = ?,
             hora_inicio = ?, hora_fin = ?, latitud = ?, longitud = ?, activo = ? WHERE id = ?`,
            [punto.nombre, punto.direccion, punto.horario, punto.dias_atencion,
             punto.hora_inicio, punto.hora_fin, punto.latitud, punto.longitud, punto.activo, id]
        );
        return result.affectedRows;
    },

    toggleActivo: async (id, activo) => {
        const [result] = await db.execute(
            'UPDATE delivery_points SET activo = ? WHERE id = ?',
            [activo, id]
        );
        return result.affectedRows;
    }
};

module.exports = DeliveryPointModel;
