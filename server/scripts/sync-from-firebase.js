// (nuevo) Reconcilia: integra a MySQL lo que quedó solo en Firestore (escrito durante una caída).
const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../.env') });

const { initializeApp, cert } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const mysql = require('mysql2/promise');
const serviceAccount = require('../firebase-service-account.json');

initializeApp({ credential: cert(serviceAccount) });
const fs = getFirestore();


const aFecha = (ms) => ms ? new Date(Number(ms)).toISOString().slice(0, 10) : null;
const aDateTime = (ms) => ms ? new Date(Number(ms)).toISOString().slice(0, 19).replace('T', ' ') : null;

// (modificado) Nombre exportable para reusar desde server.js.
async function reconcileFromFirebase() {
    const conn = await mysql.createConnection({

        host: process.env.DB_HOST,
        port: process.env.DB_PORT || 3306,
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        database: process.env.DB_NAME
    });

    // (nuevo) Asegura que un usuario exista para no violar FK.
    async function ensureUser(uid) {
        if (!uid) return;
        await conn.execute(
            'INSERT IGNORE INTO users (firebase_uid, nombre, correo, rol) VALUES (?, ?, ?, ?)',
            [uid, 'Sincronizado', uid + '@sync.local', 'Donante']
        );
    }
    // (nuevo) Asegura que una campaña exista para no violar FK.
    async function ensureCampaign(id, titulo) {
        if (!id) return;
        await conn.execute(
            'INSERT IGNORE INTO campaigns (id, titulo, descripcion, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?)',
            [id, titulo || 'Campaña sincronizada', '', aFecha(Date.now()), aFecha(Date.now()), 'Activa']
        );
    }

    let n = 0;

    // Campañas.
    const snapC = await fs.collection('campaigns').get();
    for (const doc of snapC.docs) {
        const c = doc.data();
        await ensureUser(c.creadoPor);
        const [r] = await conn.execute(
            `INSERT IGNORE INTO campaigns (id,titulo,descripcion,fecha_inicio,fecha_fin,estado,imagen_url,creado_por)
             VALUES (?,?,?,?,?,?,?,?)`,
            [doc.id, c.titulo, c.descripcion || '', c.fechaInicio, c.fechaFin, c.estado || 'Activa', c.imagenUrl || null, c.creadoPor || null]
        );
        n += r.affectedRows;
    }

    // Donaciones + historial.
    const snapD = await fs.collection('donations').get();
    for (const doc of snapD.docs) {
        const d = doc.data();
        await ensureUser(d.uid);
        await ensureCampaign(d.campaignId, d.campaignTitulo);
        const [r] = await conn.execute(
            `INSERT IGNORE INTO donations (id,user_id,campaign_id,nombre_juguete,descripcion,condicion,foto_url,estado_trazabilidad,motivo_rechazo,material_tipo,co2_ahorrado_kg)
             VALUES (?,?,?,?,?,?,?,?,?,?,?)`,
            [doc.id, d.uid, d.campaignId, d.nombre, d.descripcion || '', d.estado || 'Buen Estado',
             d.fotoUrl, d.estadoTrazabilidad || 'Pendiente', d.motivoRechazo || null,
             d.materialTipo || '', Number(d.co2AhorradoKg) || 0]
        );
        if (r.affectedRows > 0 && Array.isArray(d.historialEstados)) {
            for (const h of d.historialEstados) {
                await ensureUser(h.cambiadoPor);
                await conn.execute(
                    'INSERT IGNORE INTO donation_history (donation_id, estado, cambiado_por, fecha_cambio) VALUES (?,?,?,?)',
                    [doc.id, h.estado, h.cambiadoPor || null, aDateTime(h.fecha)]
                );
            }
        }
        n += r.affectedRows;
    }

    // Puntos de entrega.
    const snapP = await fs.collection('deliveryPoints').get();
    for (const doc of snapP.docs) {
        const p = doc.data();
        await ensureUser(p.creadoPor);
        const [r] = await conn.execute(
            `INSERT IGNORE INTO delivery_points (id,nombre,direccion,horario,dias_atencion,hora_inicio,hora_fin,latitud,longitud,activo,creado_por)
             VALUES (?,?,?,?,?,?,?,?,?,?,?)`,
            [doc.id, p.nombre, p.direccion, p.horario || '', (p.dias || []).join(','),
             p.horaInicio || '', p.horaFin || '', Number(p.latitud), Number(p.longitud), !!p.activo, p.creadoPor || null]
        );
        n += r.affectedRows;
    }

    // Eventos de recojo.
    const snapE = await fs.collection('pickupEvents').get();
    for (const doc of snapE.docs) {
        const e = doc.data();
        await ensureUser(e.creadoPor);
        await ensureCampaign(e.campaignId, e.campaignTitulo);
        const [r] = await conn.execute(
            `INSERT IGNORE INTO pickup_events (id,campaign_id,nombre,lugar,direccion,fecha,hora,latitud,longitud,activo,creado_por)
             VALUES (?,?,?,?,?,?,?,?,?,?,?)`,
            [doc.id, e.campaignId, e.nombre, e.lugar || '', e.direccion || '', e.fecha, e.hora,
             Number(e.latitud), Number(e.longitud), !!e.activo, e.creadoPor || null]
        );
        n += r.affectedRows;
    }

    // Galería.
    const snapG = await fs.collection('campaignGallery').get();
    for (const doc of snapG.docs) {
        const g = doc.data();
        await ensureUser(g.subidoPor);
        await ensureCampaign(g.campaignId, g.campaignTitulo);
        const [r] = await conn.execute(
            'INSERT IGNORE INTO campaign_gallery (id,campaign_id,foto_url,descripcion,subido_por,fecha_subida) VALUES (?,?,?,?,?,?)',
            [doc.id, g.campaignId, g.fotoUrl, g.descripcion || '', g.subidoPor || null, aDateTime(g.fechaSubida)]
        );
        n += r.affectedRows;
    }

    // Notificaciones.
    const snapN = await fs.collection('notifications').get();
    for (const doc of snapN.docs) {
        const x = doc.data();
        await ensureUser(x.uidDestinatario);
        const [r] = await conn.execute(
            'INSERT IGNORE INTO notifications (id,user_id,titulo,mensaje,donation_id,tipo,leida,fecha_creacion) VALUES (?,?,?,?,?,?,?,?)',
            [doc.id, x.uidDestinatario, x.titulo, x.mensaje, x.donacionId || null, x.tipo || 'general', !!x.leida, aDateTime(x.fechaCreacion)]
        );
        n += r.affectedRows;
    }

    await conn.end();
    console.log(`✅ Reconciliación completada: ${n} registros integrados a MySQL`);
    return n;
}

// (modificado) Si se ejecuta directo (npm run sync:from-firebase) → corre y sale.
// Si se importa desde server.js → solo exporta la función.
if (require.main === module) {
    reconcileFromFirebase()
        .then(() => process.exit(0))
        .catch(e => { console.error('❌', e); process.exit(1); });
}

module.exports = { reconcileFromFirebase };
