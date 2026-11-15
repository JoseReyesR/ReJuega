// Rutas de materiales
const express = require('express');
const router = express.Router();
const MaterialController = require('../controllers/materialController');

// GET /api/materiales - Obtener todos los materiales
router.get('/', MaterialController.getAll);

// GET /api/materiales/:id - Obtener material por ID
router.get('/:id', MaterialController.getById);

// POST /api/materiales - Crear nuevo material
router.post('/', MaterialController.create);

// PUT /api/materiales/:id - Actualizar material
router.put('/:id', MaterialController.update);

// DELETE /api/materiales/:id - Eliminar material
router.delete('/:id', MaterialController.delete);

module.exports = router;
