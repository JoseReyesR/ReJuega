// Rutas de campañas
const express = require('express');
const router = express.Router();
const CampaignController = require('../controllers/campaignController');

router.get('/', CampaignController.getAll);
router.get('/:id', CampaignController.getById);
router.post('/', CampaignController.create);

module.exports = router;
