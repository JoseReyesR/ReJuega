-- =====================================================
-- REJUEGA - BASE DE DATOS MYSQL 8.0
-- Script modular: cada sección es independiente
-- =====================================================

-- =====================================================
-- SECCIÓN 1: CREACIÓN DE BASE DE DATOS
-- =====================================================
DROP DATABASE IF EXISTS rejuega_db;
CREATE DATABASE rejuega_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rejuega_db;

-- =====================================================
-- SECCIÓN 2: TABLAS PRINCIPALES
-- =====================================================

-- 2.1 Usuarios (la autenticación real la maneja Firebase)
CREATE TABLE users (
    firebase_uid VARCHAR(128) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    rol ENUM('Donante', 'Administrador') NOT NULL DEFAULT 'Donante',
    direccion TEXT,
    telefono VARCHAR(20),
    ciudad VARCHAR(50),
    genero VARCHAR(20),
    fcm_token TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2.2 Materiales (catálogo para huella ecológica)
CREATE TABLE materials (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_material VARCHAR(50) NOT NULL,
    co2_ahorrado_kg DECIMAL(5,2) NOT NULL,
    descripcion_impacto TEXT,
    imagen_url TEXT
) ENGINE=InnoDB;

-- 2.3 Campañas
CREATE TABLE campaigns (
    id VARCHAR(36) PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado ENUM('Activa', 'Cerrada') NOT NULL DEFAULT 'Activa',
    imagen_url TEXT,
    creado_por VARCHAR(128),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creado_por) REFERENCES users(firebase_uid) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2.4 Donaciones
CREATE TABLE donations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    campaign_id VARCHAR(36) NOT NULL,
    nombre_juguete VARCHAR(100) NOT NULL,
    descripcion TEXT,
    condicion ENUM('Nuevo', 'Buen Estado') NOT NULL,
    foto_url TEXT NOT NULL,
    estado_trazabilidad ENUM('Pendiente', 'En Revision', 'Aprobado', 'Entregado', 'Rechazado') NOT NULL DEFAULT 'Pendiente',
    motivo_rechazo TEXT,
    material_tipo VARCHAR(50),
    co2_ahorrado_kg DECIMAL(5,2) DEFAULT 0.00,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(firebase_uid) ON DELETE CASCADE,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 2.5 Historial de trazabilidad (inmutable)
CREATE TABLE donation_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donation_id VARCHAR(36) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    cambiado_por VARCHAR(128),
    fecha_cambio DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donation_id) REFERENCES donations(id) ON DELETE CASCADE,
    FOREIGN KEY (cambiado_por) REFERENCES users(firebase_uid) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2.6 Puntos fijos de entrega
CREATE TABLE delivery_points (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion TEXT NOT NULL,
    horario VARCHAR(100),
    dias_atencion VARCHAR(50),
    hora_inicio VARCHAR(10),
    hora_fin VARCHAR(10),
    latitud DECIMAL(10, 7) NOT NULL,
    longitud DECIMAL(10, 7) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    creado_por VARCHAR(128),
    FOREIGN KEY (creado_por) REFERENCES users(firebase_uid) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2.7 Eventos de recojo masivo
CREATE TABLE pickup_events (
    id VARCHAR(36) PRIMARY KEY,
    campaign_id VARCHAR(36) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    lugar VARCHAR(100),
    direccion TEXT,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    latitud DECIMAL(10, 7) NOT NULL,
    longitud DECIMAL(10, 7) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    creado_por VARCHAR(128),
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (creado_por) REFERENCES users(firebase_uid) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2.8 Galería de evidencias
CREATE TABLE campaign_gallery (
    id VARCHAR(36) PRIMARY KEY,
    campaign_id VARCHAR(36) NOT NULL,
    foto_url TEXT NOT NULL,
    descripcion TEXT,
    subido_por VARCHAR(128),
    fecha_subida DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE,
    FOREIGN KEY (subido_por) REFERENCES users(firebase_uid) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 2.9 Notificaciones
CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(128) NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    mensaje TEXT NOT NULL,
    donation_id VARCHAR(36),
    tipo ENUM('estado', 'rechazo', 'general') DEFAULT 'general',
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(firebase_uid) ON DELETE CASCADE,
    FOREIGN KEY (donation_id) REFERENCES donations(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================
-- SECCIÓN 3: ÍNDICES PARA RENDIMIENTO
-- =====================================================
CREATE INDEX idx_donations_user ON donations(user_id);
CREATE INDEX idx_donations_campaign ON donations(campaign_id);
CREATE INDEX idx_donations_estado ON donations(estado_trazabilidad);
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_leida ON notifications(leida);
CREATE INDEX idx_campaigns_estado ON campaigns(estado);
CREATE INDEX idx_pickup_events_campaign ON pickup_events(campaign_id);

-- =====================================================
-- SECCIÓN 4: DATOS SEMILLA - USUARIOS
-- =====================================================
-- ⚠️ Reemplaza los firebase_uid con los reales de tu Firebase Console
INSERT INTO users (firebase_uid, nombre, correo, rol, ciudad, telefono, direccion, genero) VALUES
-- Administradores
('Ayt5Te7pZnd8PDf7nzO1b7kCBxx1', 'Admin Principal', 'admin@rejuega.app', 'Administrador', 'Trujillo', '999888777', 'Av. España 1234', 'Masculino'),
('yPF3eKmDSaW7qcCd0yJ2WEcrlY93', 'Admin Secundario', 'admin2@rejuega.app', 'Administrador', 'Lima', '999888778', 'Jr. Cusco 456', 'Femenino'),
-- Donantes
('nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'Donante Prueba', 'donante@rejuega.app', 'Donante', 'Trujillo', '999111222', 'Av. América 100', 'Femenino'),
('rAxn9a0bTMSLmrBztM8GQrb8l4V2', 'María García López', 'maria.garcia@correo.com', 'Donante', 'Trujillo', '987654321', 'Calle Los Pinos 234', 'Femenino'),
('jASUEPn8r8OJCCE8rDGMhWn3fxu2', 'Carlos Mendoza Ruiz', 'carlos.mendoza@correo.com', 'Donante', 'Lima', '987654322', 'Av. Javier Prado 567', 'Masculino'),
('bBrdlUGRUXYKUupvklpr8Abxe6s2', 'Ana Torres Vargas', 'ana.torres@correo.com', 'Donante', 'Trujillo', '987654323', 'Jr. Pizarro 890', 'Femenino'),
('dx6Q1ynS5HQz1YSDBKF6ACac5nI3', 'Luis Ramírez Paredes', 'luis.ramirez@correo.com', 'Donante', 'Chiclayo', '987654324', 'Av. Salaverry 123', 'Masculino'),
('POWGjAklUIP7XWVfg3LOgyATYpv1', 'Sofía Herrera Díaz', 'sofia.herrera@correo.com', 'Donante', 'Trujillo', '987654325', 'Calle San Martín 456', 'Femenino');

-- =====================================================
-- SECCIÓN 5: DATOS SEMILLA - MATERIALES
-- =====================================================
INSERT INTO materials (tipo_material, co2_ahorrado_kg, descripcion_impacto, imagen_url) VALUES
('Plástico ABS', 2.50, 'Reutilizar este juguete evita la emisión de 2.5 kg de CO₂ a la atmósfera.', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=300'),
('Madera', 1.80, 'Reutilizar este juguete evita la emisión de 1.8 kg de CO₂ y preserva los bosques.', 'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=300'),
('Metal', 3.20, 'Reutilizar este juguete evita la emisión de 3.2 kg de CO₂ y reduce la minería.', 'https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=300'),
('Peluche / Textil', 1.50, 'Reutilizar este juguete evita la emisión de 1.5 kg de CO₂ y reduce residuos textiles.', 'https://images.unsplash.com/photo-1559715541-5daf8a0296d0?w=300'),
('Cartón / Papel', 0.90, 'Reutilizar este juguete evita la emisión de 0.9 kg de CO₂ y salva árboles.', 'https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=300'),
('Silicona / Goma', 2.10, 'Reutilizar este juguete evita la emisión de 2.1 kg de CO₂ y reduce plásticos no reciclables.', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=300'),
('Tela reciclada', 1.20, 'Reutilizar este juguete evita la emisión de 1.2 kg de CO₂ y promueve la economía circular.', 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=300');

-- =====================================================
-- SECCIÓN 6: DATOS SEMILLA - CAMPAÑAS (5)
-- =====================================================
INSERT INTO campaigns (id, titulo, descripcion, fecha_inicio, fecha_fin, estado, imagen_url, creado_por) VALUES
('camp_001', 'Navidad Circular 2026', 'Campaña de recolección de juguetes para comedores populares de Trujillo. Todos los juguetes serán revisados y entregados a niños en situación vulnerable.', '2026-11-01', '2026-12-24', 'Activa', 'https://images.unsplash.com/photo-1513407030348-c983a97b98d8?w=600', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('camp_002', 'ReJuega en tu Colegio', 'Recolección en instituciones educativas de la región La Libertad. Los estudiantes traerán juguetes en buen estado para donar.', '2026-10-15', '2026-11-30', 'Cerrada', 'https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=600', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('camp_003', 'Día del Niño Solidario 2027', 'Campaña especial para el Día del Niño. Recolección de juguetes educativos y didácticos para comunidades rurales.', '2027-04-01', '2027-04-30', 'Activa', 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=600', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('camp_004', 'Juguetes que Vuelan', 'Campaña de recolección de juguetes para niños en hospitales pediátricos. Buscamos juguetes que alegren su estancia.', '2026-12-01', '2027-01-15', 'Activa', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=600', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('camp_005', 'ReJuega Verano 2027', 'Campaña de verano para recolectar juguetes de playa y aire libre. Donaciones para comunidades costeras.', '2027-01-15', '2027-02-28', 'Activa', 'https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=600', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93');

-- =====================================================
-- SECCIÓN 7: DATOS SEMILLA - PUNTOS DE ENTREGA (3)
-- =====================================================
INSERT INTO delivery_points (id, nombre, direccion, horario, dias_atencion, hora_inicio, hora_fin, latitud, longitud, activo, creado_por) VALUES
('point_001', 'Sede Fundación Juguete Vivo', 'Av. España 1234, Trujillo', 'Lun, Mié, Vie de 09:00 a 18:00', 'Lun,Mié,Vie', '09:00', '18:00', -8.1116, -79.0288, TRUE, 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('point_002', 'Centro Comunitario El Porvenir', 'Av. América Sur 567, El Porvenir, Trujillo', 'Mar, Jue de 10:00 a 17:00', 'Mar,Jue', '10:00', '17:00', -8.1450, -79.0350, TRUE, 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('point_003', 'Municipalidad de La Esperanza', 'Jr. Los Olivos 890, La Esperanza, Trujillo', 'Lun a Vie de 08:00 a 16:00', 'Lun,Mar,Mié,Jue,Vie', '08:00', '16:00', -8.0890, -79.0420, TRUE, 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93');

-- =====================================================
-- SECCIÓN 8: DATOS SEMILLA - EVENTOS DE RECOJO (3)
-- =====================================================
INSERT INTO pickup_events (id, campaign_id, nombre, lugar, direccion, fecha, hora, latitud, longitud, activo, creado_por) VALUES
('event_001', 'camp_001', 'Recojo masivo Navidad Circular', 'Parque Central de Trujillo', 'Jr. Pizarro 500, Trujillo', '2026-12-15', '10:00:00', -8.1120, -79.0280, TRUE, 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('event_002', 'camp_001', 'Recojo en Mercado Mayorista', 'Mercado Mayorista de Trujillo', 'Av. España 2000, Trujillo', '2026-12-20', '14:00:00', -8.1150, -79.0320, TRUE, 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('event_003', 'camp_003', 'Recojo escolar Día del Niño', 'Colegio Nacional San Juan', 'Av. América 1000, Trujillo', '2027-04-15', '09:00:00', -8.1090, -79.0250, TRUE, 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93');

-- =====================================================
-- SECCIÓN 9: DATOS SEMILLA - DONACIONES (10+)
-- =====================================================
INSERT INTO donations (id, user_id, campaign_id, nombre_juguete, descripcion, condicion, foto_url, estado_trazabilidad, material_tipo, co2_ahorrado_kg) VALUES
-- Donaciones del donante de prueba
('don_001', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'camp_001', 'Carrito de madera', 'Carrito artesanal en buen estado, pintado a mano.', 'Buen Estado', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', 'Aprobado', 'Madera', 1.80),
('don_002', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'camp_001', 'Muñeca de peluche', 'Muñeca de peluche limpia, sin roturas.', 'Nuevo', 'https://images.unsplash.com/photo-1559715541-5daf8a0296d0?w=400', 'En Revision', 'Peluche / Textil', 1.50),
('don_003', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'camp_002', 'Bloques de construcción', 'Set completo con caja original, 120 piezas.', 'Buen Estado', 'https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=400', 'Entregado', 'Plástico ABS', 2.50),
-- Donaciones de otros donantes
('don_004', 'rAxn9a0bTMSLmrBztM8GQrb8l4V2', 'camp_001', 'Oso de peluche grande', 'Oso de peluche tamaño mediano, muy suave.', 'Buen Estado', 'https://images.unsplash.com/photo-1559715541-5daf8a0296d0?w=400', 'Pendiente', 'Peluche / Textil', 1.50),
('don_005', 'jASUEPn8r8OJCCE8rDGMhWn3fxu2', 'camp_003', 'Rompecabezas educativo', 'Rompecabezas de 100 piezas, tema animales.', 'Nuevo', 'https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=400', 'Aprobado', 'Cartón / Papel', 0.90),
('don_006', 'bBrdlUGRUXYKUupvklpr8Abxe6s2', 'camp_004', 'Auto de metal a escala', 'Auto de colección en perfecto estado.', 'Nuevo', 'https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=400', 'En Revision', 'Metal', 3.20),
('don_007', 'dx6Q1ynS5HQz1YSDBKF6ACac5nI3', 'camp_005', 'Pelota de silicona', 'Pelota rebotadora de silicona, como nueva.', 'Buen Estado', 'https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=400', 'Rechazado', 'Silicona / Goma', 2.10),
('don_008', 'POWGjAklUIP7XWVfg3LOgyATYpv1', 'camp_001', 'Muñeco de acción', 'Muñeco de acción con todos sus accesorios.', 'Buen Estado', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400', 'Pendiente', 'Plástico ABS', 2.50),
('don_009', 'rAxn9a0bTMSLmrBztM8GQrb8l4V2', 'camp_003', 'Libro de cuentos', 'Libro de cuentos ilustrados para niños de 3-5 años.', 'Nuevo', 'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?w=400', 'Aprobado', 'Cartón / Papel', 0.90),
('don_010', 'jASUEPn8r8OJCCE8rDGMhWn3fxu2', 'camp_004', 'Tela reciclada - Muñeco', 'Muñeco hecho a mano con tela reciclada.', 'Buen Estado', 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=400', 'Entregado', 'Tela reciclada', 1.20);

-- =====================================================
-- SECCIÓN 10: DATOS SEMILLA - HISTORIAL DE TRAZABILIDAD
-- =====================================================
INSERT INTO donation_history (donation_id, estado, cambiado_por) VALUES
('don_001', 'Pendiente', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1'),
('don_001', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_001', 'Aprobado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_002', 'Pendiente', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1'),
('don_002', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_003', 'Pendiente', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1'),
('don_003', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_003', 'Aprobado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_003', 'Entregado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_005', 'Pendiente', 'jASUEPn8r8OJCCE8rDGMhWn3fxu2'),
('don_005', 'En Revision', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('don_005', 'Aprobado', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('don_006', 'Pendiente', 'bBrdlUGRUXYKUupvklpr8Abxe6s2'),
('don_006', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_007', 'Pendiente', 'dx6Q1ynS5HQz1YSDBKF6ACac5nI3'),
('don_007', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_007', 'Rechazado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_009', 'Pendiente', 'rAxn9a0bTMSLmrBztM8GQrb8l4V2'),
('don_009', 'En Revision', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('don_009', 'Aprobado', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('don_010', 'Pendiente', 'jASUEPn8r8OJCCE8rDGMhWn3fxu2'),
('don_010', 'En Revision', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_010', 'Aprobado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('don_010', 'Entregado', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1');

-- =====================================================
-- SECCIÓN 11: DATOS SEMILLA - GALERÍA DE EVIDENCIAS
-- =====================================================
INSERT INTO campaign_gallery (id, campaign_id, foto_url, descripcion, subido_por) VALUES
('gal_001', 'camp_002', 'https://images.unsplash.com/photo-1513407030348-c983a97b98d8?w=600', 'Entrega de juguetes en colegio San Juan', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('gal_002', 'camp_002', 'https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=600', 'Niños recibiendo sus juguetes', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('gal_003', 'camp_002', 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?w=600', 'Voluntarios organizando donaciones', 'yPF3eKmDSaW7qcCd0yJ2WEcrlY93'),
('gal_004', 'camp_001', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600', 'Inicio de campaña Navidad Circular', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1'),
('gal_005', 'camp_001', 'https://images.unsplash.com/photo-1559715541-5daf8a0296d0?w=600', 'Recolección en centro comercial', 'Ayt5Te7pZnd8PDf7nzO1b7kCBxx1');

-- =====================================================
-- SECCIÓN 12: DATOS SEMILLA - NOTIFICACIONES
-- =====================================================
INSERT INTO notifications (id, user_id, titulo, mensaje, donation_id, tipo, leida) VALUES
('not_001', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'Tu donación fue aprobada', 'El juguete "Carrito de madera" pasó al estado Aprobado.', 'don_001', 'estado', TRUE),
('not_002', 'nl6wsvuqrRSYTu8IRyAHpG5uilp1', 'Tu donación está en revisión', 'El juguete "Muñeca de peluche" está siendo revisado por nuestro equipo.', 'don_002', 'estado', FALSE),
('not_003', 'jASUEPn8r8OJCCE8rDGMhWn3fxu2', 'Tu donación fue aprobada', 'El "Rompecabezas educativo" fue aprobado. ¡Gracias por tu aporte!', 'don_005', 'estado', TRUE),
('not_004', 'dx6Q1ynS5HQz1YSDBKF6ACac5nI3', 'Tu donación fue rechazada', 'La "Pelota de silicona" fue rechazada. Motivo: Presenta desgaste excesivo.', 'don_007', 'rechazo', TRUE),
('not_005', 'rAxn9a0bTMSLmrBztM8GQrb8l4V2', 'Tu donación fue aprobada', 'El "Libro de cuentos" fue aprobado.', 'don_009', 'estado', FALSE);

-- =====================================================
-- SECCIÓN 13: EXTENSIONES (Espacio para agregar más tablas)
-- =====================================================
-- Para agregar nuevas tablas en el futuro, hazlo aquí abajo.
-- Ejemplo:
-- CREATE TABLE feedback (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     user_id VARCHAR(128) NOT NULL,
--     comentario TEXT NOT NULL,
--     calificacion INT CHECK (calificacion BETWEEN 1 AND 5),
--     fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (user_id) REFERENCES users(firebase_uid) ON DELETE CASCADE
-- ) ENGINE=InnoDB;

-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================