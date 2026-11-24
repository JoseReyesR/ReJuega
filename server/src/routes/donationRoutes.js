// Rutas de donaciones
const express = require('express');
const router = express.Router();
const DonationController = require('../controllers/donationController');

router.get('/', DonationController.getAll);
router.get('/:id', DonationController.getById);
router.post('/', DonationController.create);
router.put('/:id', DonationController.update);
router.put('/:id/estado', DonationController.updateEstado);
router.delete('/:id', DonationController.delete);

module.exports = router;
