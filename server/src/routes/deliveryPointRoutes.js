// Rutas de puntos de entrega
const express = require('express');
const router = express.Router();
const DeliveryPointController = require('../controllers/deliveryPointController');

router.get('/', DeliveryPointController.getAll);
router.get('/:id', DeliveryPointController.getById);
router.post('/', DeliveryPointController.create);
router.put('/:id', DeliveryPointController.update);
router.put('/:id/toggle', DeliveryPointController.toggleActivo);

module.exports = router;
