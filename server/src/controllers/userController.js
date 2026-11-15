// Controlador de usuarios
const UserModel = require('../models/userModel');

const UserController = {
    // GET /api/users
    getAll: async (req, res) => {
        try {
            const { rol } = req.query;
            const users = await UserModel.getAll(rol);

            res.status(200).json({
                success: true,
                count: users.length,
                data: users
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener usuarios',
                details: error.message
            });
        }
    },

    // GET /api/users/:uid
    getByUid: async (req, res) => {
        try {
            const { uid } = req.params;
            const user = await UserModel.getByUid(uid);

            if (!user) {
                return res.status(404).json({
                    success: false,
                    error: 'Usuario no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                data: user
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al obtener usuario',
                details: error.message
            });
        }
    },

    // POST /api/users
    create: async (req, res) => {
        try {
            const { firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero } = req.body;

            const requiredFields = ['firebase_uid', 'nombre', 'correo', 'rol'];
            const missingFields = requiredFields.filter(field => !req.body[field]);

            if (missingFields.length > 0) {
                return res.status(400).json({
                    success: false,
                    error: 'Campos obligatorios faltantes',
                    required: requiredFields,
                    missing: missingFields
                });
            }

            await UserModel.create(firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero);

            res.status(201).json({
                success: true,
                message: 'Usuario creado exitosamente',
                data: { firebase_uid, nombre, correo, rol }
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al crear usuario',
                details: error.message
            });
        }
    },

    // PUT /api/users/:uid
    update: async (req, res) => {
        try {
            const { uid } = req.params;
            const { nombre, ciudad, telefono, direccion, genero } = req.body;

            const datos = {};
            if (nombre) datos.nombre = nombre;
            if (ciudad) datos.ciudad = ciudad;
            if (telefono) datos.telefono = telefono;
            if (direccion) datos.direccion = direccion;
            if (genero) datos.genero = genero;

            const affected = await UserModel.update(uid, datos);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Usuario no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Perfil actualizado exitosamente'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar perfil',
                details: error.message
            });
        }
    },

    // PUT /api/users/:uid/fcm-token
    updateFcmToken: async (req, res) => {
        try {
            const { uid } = req.params;
            const { fcm_token } = req.body;

            if (!fcm_token) {
                return res.status(400).json({
                    success: false,
                    error: 'fcm_token es obligatorio'
                });
            }

            const affected = await UserModel.updateFcmToken(uid, fcm_token);

            if (affected === 0) {
                return res.status(404).json({
                    success: false,
                    error: 'Usuario no encontrado'
                });
            }

            res.status(200).json({
                success: true,
                message: 'Token FCM actualizado'
            });
        } catch (error) {
            res.status(500).json({
                success: false,
                error: 'Error al actualizar token FCM',
                details: error.message
            });
        }
    }
};

module.exports = UserController;
