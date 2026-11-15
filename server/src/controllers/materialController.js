// Controlador de materiales - Maneja la lógica de negocio
const MaterialModel = require('../models/materialModel');

const MaterialController = {
    // GET /api/materiales
    getAll: async (req, res) => {
        try {
            const materiales = await MaterialModel.getAll();
            res.status(200).json({
                success: true,
                count: materiales.length,
                data: materiales
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener materiales',
                details: error.message
            });
        }
    },

    // GET /api/materiales/:id
    getById: async (req, res) => {
        try {
            const { id } = req.params;
            const material = await MaterialModel.getById(id);

            if (!material) {
                return res.status(404).json({
                    success: false,
                    error: 'Material no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                data: material
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener material',
                details: error.message
            });
        }
    },

    // POST /api/materiales
    create: async (req, res) => {
        try {
            const { tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url } = req.body;

            // Validación de campos obligatorios
            if (!tipo_material || co2_ahorrado_kg === undefined) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: ['tipo_material', 'co2_ahorrado_kg']
                });
            }

            // Validación de tipo de dato
            if (typeof co2_ahorrado_kg !== 'number' || co2_ahorrado_kg < 0) {
                return res.status(400).json({
                    success: false,
                    error: 'co2_ahorrado_kg debe ser un número positivo'
                });
            }

            const id = await MaterialModel.create(
                tipo_material,
                co2_ahorrado_kg,
                descripcion_impacto || '',
                imagen_url || null
            );

            res.status(201).json({
                success: true,
                message: 'Material creado exitosamente',
                data: { id, tipo_material, co2_ahorrado_kg }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear material',
                details: error.message
            });
        }
    },

    // PUT /api/materiales/:id
    update: async (req, res) => {
        try {
            const { id } = req.params;
            const { tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url } = req.body;

            const affected = await MaterialModel.update(
                id, tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url
            );

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Material no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Material actualizado exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar material',
                details: error.message
            });
        }
    },

    // DELETE /api/materiales/:id
    delete: async (req, res) => {
        try {
            const { id } = req.params;
            const affected = await MaterialModel.delete(id);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Material no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Material eliminado exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al eliminar material',
                details: error.message
            });
        }
    }
};

module.exports = MaterialController;
