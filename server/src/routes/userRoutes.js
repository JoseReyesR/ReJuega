// Rutas de usuarios
const express = require('express');
const router = express.Router();
const UserController = require('../controllers/userController');

router.get('/', UserController.getAll);
router.get('/:uid', UserController.getByUid);
router.post('/', UserController.create);
router.post('/sync', UserController.syncFromFirebase);
router.put('/:uid', UserController.update);
router.put('/:uid/fcm-token', UserController.updateFcmToken);
router.put('/:uid/rol', UserController.updateRol);

module.exports = router;
