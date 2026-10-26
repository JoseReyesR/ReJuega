// (modificado) Archivo: DatosSemilla.kt
// (modificado) Descripción: Datos hardcodeados para probar la UI y poblar Room inicialmente.
// (modificado) Cambio: Se agrega lista de juguetes de prueba para la campaña 1.
package pe.rejuega.app.utils

import pe.rejuega.app.data.local.Campaign
import pe.rejuega.app.data.local.Juguete
import pe.rejuega.app.data.local.MaterialEco
import pe.rejuega.app.data.local.PuntoEntrega

// (modificado) Objeto singleton con datos de prueba.
object DatosSemilla {

    // (modificado) Lista de campañas de prueba.
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

    // (modificado) Lista de puntos de entrega físicos.
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

    // (modificado) Lista de materiales ecológicos.
    val listaMaterialesEco = listOf(
        MaterialEco(
            id = "plastico",
            tipoMaterial = "Plástico (ABS/Polietileno)",
            co2AhorradoKg = 2.5,
            descripcionImpacto = "Reutilizar este juguete evita la emisión de 2.5 kg de CO2 equivalente a la fabricación de uno nuevo."
        ),
        MaterialEco(
            id = "madera",
            tipoMaterial = "Madera y textiles",
            co2AhorradoKg = 1.8,
            descripcionImpacto = "La madera procesada requiere mucha energía. ¡Gracias por salvar árboles y reducir huella!"
        )
    )

    // (nuevo) Lista de juguetes hardcodeados para probar la base de datos local.
    // (nuevo) Estos juguetes pertenecen a la campaña 1.
    val listaJuguetes = listOf(
        Juguete(
            id = 1,
            campaignId = 1,
            nombre = "Carrito de madera",
            descripcion = "Carrito artesanal de madera en buen estado. Sin piezas sueltas.",
            estado = "Buen Estado",
            fotoUrl = "https://images.unsplash.com/photo-1509470475797-537576c435a5",
            estadoTrazabilidad = "Pendiente"
        ),
        Juguete(
            id = 2,
            campaignId = 1,
            nombre = "Muñeco de peluche",
            descripcion = "Peluche limpio, sin roturas y recién lavado.",
            estado = "Nuevo",
            fotoUrl = "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4",
            estadoTrazabilidad = "En Revision"
        ),
        Juguete(
            id = 3,
            campaignId = 1,
            nombre = "Bloques de construcción",
            descripcion = "Set completo de bloques plásticos. Incluye caja original.",
            estado = "Buen Estado",
            fotoUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9",
            estadoTrazabilidad = "Aprobado"
        )
    )
}