// Rutas de eventos de recojo
const express = require('express');
const router = express.Router();
const PickupEventController = require('../controllers/pickupEventController');

router.get('/', PickupEventController.getAll);
router.get('/:id', PickupEventController.getById);
router.post('/', PickupEventController.create);
router.put('/:id', PickupEventController.update);
router.put('/:id/toggle', PickupEventController.toggleActivo);

module.exports = router;
