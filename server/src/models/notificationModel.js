// Modelo de notificaciones
const db = require('../../db');

const NotificationModel = {
    getByUser: async (user_id, leida = null) => {
        let query = 'SELECT * FROM notifications WHERE user_id = ?';
        let params = [user_id];

        if (leida !== null) {
            query += ' AND leida = ?';
            params.push(leida);
        }

        query += ' ORDER BY fecha_creacion DESC';
        const [rows] = await db.execute(query, params);
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM notifications WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (notificacion) => {
        const [result] = await db.execute(
            'INSERT INTO notifications (id, user_id, titulo, mensaje, donation_id, tipo, leida) VALUES (?, ?, ?, ?, ?, ?, ?)',
            [notificacion.id, notificacion.user_id, notificacion.titulo, notificacion.mensaje,
             notificacion.donation_id, notificacion.tipo, notificacion.leida || false]
        );
        return result.insertId;
    },

    markAsRead: async (id) => {
        const [result] = await db.execute(
            'UPDATE notifications SET leida = TRUE WHERE id = ?',
            [id]
        );
        return result.affectedRows;
    },

    markAllAsRead: async (user_id) => {
        const [result] = await db.execute(
            'UPDATE notifications SET leida = TRUE WHERE user_id = ? AND leida = FALSE',
            [user_id]
        );
        return result.affectedRows;
    },

    countUnread: async (user_id) => {
        const [rows] = await db.execute(
            'SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND leida = FALSE',
            [user_id]
        );
        return rows[0].count;
    }
};

module.exports = NotificationModel;
