// Modelo de campañas
const db = require('../../db');

const CampaignModel = {
    getAll: async (estado = null) => {
        let query = 'SELECT * FROM campaigns';
        let params = [];

        if (estado) {
            query += ' WHERE estado = ?';
            params.push(estado);
        }

        query += ' ORDER BY fecha_creacion DESC';
        const [rows] = await db.execute(query, params);
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM campaigns WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por) => {
        const [result] = await db.execute(
            'INSERT INTO campaigns (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por]
        );
        return result.insertId;
    },

    update: async (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url) => {
        const [result] = await db.execute(
            'UPDATE campaigns SET titulo = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?, estado = ?, imagen_url = ? WHERE id = ?',
            [titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, id]
        );
        return result.affectedRows;
    }
};

module.exports = CampaignModel;
