// (nuevo) Lee MySQL y escribe en Firestore como backup inicial.
require('dotenv').config();
const admin = require('firebase-admin');
const mysql = require('mysql2/promise');
const serviceAccount = require('../firebase-service-account.json');

admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
const fs = admin.firestore();
fs.settings({ ignoreUndefinedProperties: true });

// true = NO subir users (se gestionan por Auth + sync de login).
const SKIP_USERS = true;

const aMillis = (s) => s ? new Date(String(s).replace(' ', 'T')).getTime() : Date.now();

async function main() {
    const conn = await mysql.createConnection({
        host: process.env.DB_HOST, user: process.env.DB_USER,
        password: process.env.DB_PASSWORD, database: process.env.DB_NAME,
        dateStrings: true
    });

    // Campañas (mapa id -> titulo para referencias).
    const [campaigns] = await conn.execute('SELECT * FROM campaigns');
    const titulos = {};
    for (const c of campaigns) titulos[c.id] = c.titulo;

    if (!SKIP_USERS) {
        const [users] = await conn.execute('SELECT * FROM users');
        for (const u of users) {
            await fs.collection('users').doc(u.firebase_uid).set({
                uid: u.firebase_uid, correo: u.correo, nombre: u.nombre, rol: u.rol,
                direccion: u.direccion || '', telefono: u.telefono || '',
                ciudad: u.ciudad || '', genero: u.genero || '',
                fcmToken: u.fcm_token || '', fechaRegistro: aMillis(u.fecha_registro)
            });
        }
        console.log('users:', users.length);
    }

    for (const c of campaigns) {
        await fs.collection('campaigns').doc(c.id).set({
            titulo: c.titulo, descripcion: c.descripcion || '',
            fechaInicio: c.fecha_inicio, fechaFin: c.fecha_fin,
            estado: c.estado, imagenUrl: c.imagen_url || '',
            creadoPor: c.creado_por || '', fechaCreacion: aMillis(c.fecha_creacion)
        });
    }
    console.log('campaigns:', campaigns.length);

    // Donaciones + historial.
    const [donations] = await conn.execute('SELECT * FROM donations');
    const [history] = await conn.execute('SELECT * FROM donation_history ORDER BY fecha_cambio ASC');
    const hist = {};
    for (const h of history) {
        (hist[h.donation_id] = hist[h.donation_id] || []).push({
            estado: h.estado, fecha: aMillis(h.fecha_cambio), cambiadoPor: h.cambiado_por || ''
        });
    }
    for (const d of donations) {
        await fs.collection('donations').doc(d.id).set({
            id: d.id, uid: d.user_id, campaignId: 0,
            campaignTitulo: titulos[d.campaign_id] || '',
            nombre: d.nombre_juguete, descripcion: d.descripcion || '',
            estado: d.condicion, fotoUrl: d.foto_url,
            estadoTrazabilidad: d.estado_trazabilidad,
            motivoRechazo: d.motivo_rechazo || null,
            materialTipo: d.material_tipo || '', co2AhorradoKg: Number(d.co2_ahorrado_kg) || 0,
            historialEstados: hist[d.id] || [],
            fechaRegistro: aMillis(d.fecha_registro)
        });
    }
    console.log('donations:', donations.length);

    const [points] = await conn.execute('SELECT * FROM delivery_points');
    for (const p of points) {
        await fs.collection('deliveryPoints').doc(p.id).set({
            id: p.id, nombre: p.nombre, direccion: p.direccion, horario: p.horario || '',
            dias: (p.dias_atencion || '').split(',').filter(Boolean),
            horaInicio: p.hora_inicio || '', horaFin: p.hora_fin || '',
            latitud: Number(p.latitud), longitud: Number(p.longitud),
            activo: !!p.activo, creadoPor: p.creado_por || ''
        });
    }
    console.log('deliveryPoints:', points.length);

    const [events] = await conn.execute('SELECT * FROM pickup_events');
    for (const e of events) {
        await fs.collection('pickupEvents').doc(e.id).set({
            id: e.id, campaignId: e.campaign_id, campaignTitulo: titulos[e.campaign_id] || '',
            nombre: e.nombre, lugar: e.lugar || '', direccion: e.direccion || '',
            fecha: e.fecha, hora: e.hora,
            latitud: Number(e.latitud), longitud: Number(e.longitud),
            activo: !!e.activo, creadoPor: e.creado_por || ''
        });
    }
    console.log('pickupEvents:', events.length);

    const [gallery] = await conn.execute('SELECT * FROM campaign_gallery');
    for (const g of gallery) {
        await fs.collection('campaignGallery').doc(g.id).set({
            id: g.id, campaignId: g.campaign_id, campaignTitulo: titulos[g.campaign_id] || '',
            fotoUrl: g.foto_url, descripcion: g.descripcion || '',
            fechaSubida: aMillis(g.fecha_subida), subidoPor: g.subido_por || ''
        });
    }
    console.log('campaignGallery:', gallery.length);

    const [notifs] = await conn.execute('SELECT * FROM notifications');
    for (const n of notifs) {
        await fs.collection('notifications').doc(n.id).set({
            id: n.id, uidDestinatario: n.user_id, titulo: n.titulo, mensaje: n.mensaje,
            donacionId: n.donation_id || '', tipo: n.tipo || 'general',
            leida: !!n.leida, fechaCreacion: aMillis(n.fecha_creacion)
        });
    }
    console.log('notifications:', notifs.length);

    await conn.end();
    console.log('✅ Seed MySQL → Firestore completado');
    process.exit(0);
}

main().catch(e => { console.error('❌', e); process.exit(1); });
