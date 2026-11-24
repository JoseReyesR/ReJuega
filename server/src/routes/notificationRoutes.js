// Rutas de notificaciones
const express = require('express');
const router = express.Router();
const NotificationController = require('../controllers/notificationController');

router.get('/user/:user_id', NotificationController.getByUser);
router.post('/', NotificationController.create);
router.put('/:id/read', NotificationController.markAsRead);
router.put('/user/:user_id/read-all', NotificationController.markAllAsRead);
router.get('/user/:user_id/unread-count', NotificationController.countUnread);

module.exports = router;
