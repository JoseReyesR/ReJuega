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

    // (nuevo) Verifica que el UID exista en users para no violar la FK.
    usuarioExiste: async (uid) => {
        if (!uid) return false;
        const [rows] = await db.execute(
            'SELECT firebase_uid FROM users WHERE firebase_uid = ?', [uid]
        );
        return rows.length > 0;
    },

    // (modificado) Si creado_por no existe en users, se guarda NULL (evita error de FK).
    create: async (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por) => {
        const creadoValido = (await CampaignModel.usuarioExiste(creado_por)) ? creado_por : null;
        const [result] = await db.execute(
            'INSERT INTO campaigns (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creadoValido]
        );
        return result.insertId;
    },

    update: async (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url) => {
        const [result] = await db.execute(
            'UPDATE campaigns SET titulo = ?, descripcion = ?, fecha_inicio = ?, fecha_fin = ?, estado = ?, imagen_url = ? WHERE id = ?',
            [titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, id]
        );
        return result.affectedRows;
    },

    updateEstado: async (id, estado) => {
        const [result] = await db.execute('UPDATE campaigns SET estado = ? WHERE id = ?', [estado, id]);
        return result.affectedRows;
    }
};

module.exports = CampaignModel;
