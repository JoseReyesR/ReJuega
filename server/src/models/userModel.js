// Modelo de usuarios
const db = require('../../db');

const UserModel = {
    getAll: async (rol = null) => {
        let query = 'SELECT firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero, fecha_registro FROM users';
        let params = [];

        if (rol) {
            query += ' WHERE rol = ?';
            params.push(rol);
        }

        query += ' ORDER BY fecha_registro DESC';
        const [rows] = await db.execute(query, params);
        return rows;
    },

    getByUid: async (firebase_uid) => {
        const [rows] = await db.execute(
            'SELECT firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero, fcm_token, fecha_registro FROM users WHERE firebase_uid = ?',
            [firebase_uid]
        );
        return rows[0];
    },

    create: async (firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero) => {
        const [result] = await db.execute(
            'INSERT INTO users (firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero]
        );
        return result.insertId;
    },

    update: async (firebase_uid, datos) => {
        const campos = Object.keys(datos);
        const valores = Object.values(datos);

        const setClause = campos.map(campo => `${campo} = ?`).join(', ');
        const query = `UPDATE users SET ${setClause} WHERE firebase_uid = ?`;

        const [result] = await db.execute(query, [...valores, firebase_uid]);
        return result.affectedRows;
    },

    updateFcmToken: async (firebase_uid, fcm_token) => {
        const [result] = await db.execute(
            'UPDATE users SET fcm_token = ? WHERE firebase_uid = ?',
            [fcm_token, firebase_uid]
        );
        return result.affectedRows;
    },

    updateRol: async (firebase_uid, rol) => {
        const [result] = await db.execute(
            'UPDATE users SET rol = ? WHERE firebase_uid = ?', [rol, firebase_uid]
        );
        return result.affectedRows;
    },

    getByCorreo: async (correo) => {
        const [rows] = await db.execute('SELECT * FROM users WHERE correo = ?', [correo]);
        return rows[0];
    }
};

module.exports = UserModel;
