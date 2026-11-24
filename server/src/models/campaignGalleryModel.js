// Modelo de galería de campañas
const db = require('../../db');

const CampaignGalleryModel = {
    getByCampaign: async (campaign_id) => {
        const [rows] = await db.execute(
            'SELECT * FROM campaign_gallery WHERE campaign_id = ? ORDER BY fecha_subida DESC',
            [campaign_id]
        );
        return rows;
    },

    getById: async (id) => {
        const [rows] = await db.execute('SELECT * FROM campaign_gallery WHERE id = ?', [id]);
        return rows[0];
    },

    create: async (evidencia) => {
        const [result] = await db.execute(
            'INSERT INTO campaign_gallery (id, campaign_id, foto_url, descripcion, subido_por) VALUES (?, ?, ?, ?, ?)',
            [evidencia.id, evidencia.campaign_id, evidencia.foto_url, evidencia.descripcion, evidencia.subido_por]
        );
        return result.insertId;
    },

    delete: async (id) => {
        const [result] = await db.execute('DELETE FROM campaign_gallery WHERE id = ?', [id]);
        return result.affectedRows;
    }
};

module.exports = CampaignGalleryModel;
