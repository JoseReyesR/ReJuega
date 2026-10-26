// (modificado) Archivo: DatosSemilla.kt
// (modificado) Descripción: Datos hardcodeados para probar la UI y poblar Room inicialmente.
// (modificado) Motivo: Alineación con los nombres de entidades (Español + Campaign).
package pe.rejuega.app.utils

import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.local.MaterialEco
import pe.rejuega.app.data.local.PuntoEntrega

object DatosSemilla {

    // (modificado) Lista de campañas de prueba (Alineado a ODS 12)
    val listaCampaigns = listOf(
        Campaign(
            id = 1,
            titulo = "Navidad Circular 2026",
            descripcion = "Campaña de recolección de juguetes para niños en comedores populares de Trujillo. ¡Dale una segunda vida a tus juguetes!",
            fechaInicio = "2026-11-01",
            fechaFin = "2026-12-24",
            estado = "Activa",
            imagenUrl = "https://images.unsplash.com/photo-1513407030348-c983a97b98d8"
        ),
        Campaign(
            id = 2,
            titulo = "ReJuega en tu Colegio",
            descripcion = "Iniciativa de concientización y recolección en instituciones educativas. Fomentando el consumo responsable desde niños.",
            fechaInicio = "2026-10-15",
            fechaFin = "2026-11-30",
            estado = "Activa",
            imagenUrl = "https://images.unsplash.com/photo-1596464716127-f2a82984de30"
        )
    )

    // (modificado) Lista de puntos de entrega físicos (Para el Mapa de Google Maps)
    val listaPuntosEntrega = listOf(
        PuntoEntrega(
            id = 1,
            nombre = "Sede Fundación Juguete Vivo",
            direccion = "Av. España 1234, Trujillo",
            horario = "Lun-Vie 9:00 am - 6:00 pm",
            latitud = -8.1116,
            longitud = -79.0288,
            activo = true
        ),
        PuntoEntrega(
            id = 2,
            nombre = "Centro Comercial Real Plaza (Módulo ONG)",
            direccion = "Av. Fátima, Trujillo",
            horario = "Lun-Dom 10:00 am - 9:00 pm",
            latitud = -8.1250,
            longitud = -79.0350,
            activo = true
        )
    )

    // (modificado) Datos de impacto ambiental (Simula lo que luego vendrá de MockAPI.io)
    val listaMaterialesEco = listOf(
        MaterialEco(
            id = "plastico",
            tipoMaterial = "Plástico (ABS/Polietileno)",
            co2AhorradoKg = 2.5,
            descripcionImpacto = "Reutilizar este juguete evita la emisión de 2.5 kg de CO2 equivalents a la fabricación de uno nuevo."
        ),
        MaterialEco(
            id = "madera",
            tipoMaterial = "Madera y textiles",
            co2AhorradoKg = 1.8,
            descripcionImpacto = "La madera procesada requiere mucha energía. ¡Gracias por salvar árboles y reducir huella!"
        )
    )
}