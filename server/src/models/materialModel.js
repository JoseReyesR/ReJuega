// Modelo de materiales - Consultas SQL
const db = require('../../db');

const MaterialModel = {
    // Obtener todos los materiales
    getAll: async () => {
        const [rows] = await db.execute('SELECT * FROM materials ORDER BY id');
        return rows;
    },

    // Obtener material por ID
    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM materials WHERE id = ?', [id]);
        return rows[0];
    },

    // Crear nuevo material
    create: async (tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url) => {
        const [result] = await db.execute(
            'INSERT INTO materials (tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url) VALUES (?, ?, ?, ?)',
            [tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url]
        );
        return result.insertId;
    },

    // Actualizar material
    update: async (id, tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url) => {
        const [result] = await db.execute(
            'UPDATE materials SET tipo_material = ?, co2_ahorrado_kg = ?, descripcion_impacto = ?, imagen_url = ? WHERE id = ?',
            [tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url, id]
        );
        return result.affectedRows;
    },

    // Eliminar material
    delete: async (id) => {
        const [result] = await db.execute('DELETE FROM materials WHERE id = ?', [id]);
        return result.affectedRows;
    }
};

module.exports = MaterialModel;
