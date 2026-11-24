// Rutas de galería de campañas
const express = require('express');
const router = express.Router();
const CampaignGalleryController = require('../controllers/campaignGalleryController');

router.get('/campaign/:campaign_id', CampaignGalleryController.getByCampaign);
router.post('/', CampaignGalleryController.create);
router.delete('/:id', CampaignGalleryController.delete);

module.exports = router;
