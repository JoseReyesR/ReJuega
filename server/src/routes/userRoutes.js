// Rutas de usuarios
const express = require('express');
const router = express.Router();
const UserController = require('../controllers/userController');

router.get('/', UserController.getAll);
router.get('/:uid', UserController.getByUid);
router.post('/', UserController.create);
router.put('/:uid', UserController.update);
router.put('/:uid/fcm-token', UserController.updateFcmToken);

module.exports = router;
