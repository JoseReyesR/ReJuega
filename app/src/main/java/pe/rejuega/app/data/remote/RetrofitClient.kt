// (nuevo) Archivo: RetrofitClient.kt
// (nuevo) Descripción: Configuración central del cliente Retrofit para consumir MockAPI.
// (nuevo) Patrón Singleton: solo existe una instancia en toda la app para ahorrar recursos.
package pe.rejuega.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// (nuevo) Objeto singleton que proporciona la instancia configurada de Retrofit.
object RetrofitClient {

    // (nuevo) URL base de MockAPI.io.
    // (nuevo) IMPORTANTE: Reemplazar  URL con la de mockapi
    // (nuevo) Debe terminar con "/" (barra al final).
    private const val BASE_URL = "https://6ae016288e42963c8d5edb8e.mockapi.io/"

    // (nuevo) Interceptor de logging para ver las peticiones HTTP en Logcat.
    // (nuevo) Level.BODY muestra el contenido completo de las peticiones y respuestas.
    // (nuevo) En producción, cambiar a Level.NONE para no exponer datos sensibles.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // (nuevo) Cliente OkHttp configurado con timeouts y el interceptor de logs.
    private val okHttpClient = OkHttpClient.Builder()
        // (nuevo) Timeout de conexión: 30 segundos antes de fallar si no hay respuesta.
        .connectTimeout(30, TimeUnit.SECONDS)
        // (nuevo) Timeout de lectura: 30 segundos para recibir la respuesta completa.
        .readTimeout(30, TimeUnit.SECONDS)
        // (nuevo) Timeout de escritura: 30 segundos para enviar la petición.
        .writeTimeout(30, TimeUnit.SECONDS)
        // (nuevo) Agregamos el interceptor para ver logs en Logcat.
        .addInterceptor(loggingInterceptor)
        .build()

    // (nuevo) Instancia de Retrofit configurada con la URL base, el cliente OkHttp y el converter.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            // (nuevo) URL base de la API.
            .baseUrl(BASE_URL)
            // (nuevo) Cliente OkHttp con timeouts y logging.
            .client(okHttpClient)
            // (nuevo) Converter Gson: convierte JSON a objetos Kotlin automáticamente.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // (nuevo) Metodo público para obtener la instancia del servicio.
    // (nuevo) Retrofit genera automáticamente la implementación de la interfaz.
    fun getMaterialJugueteService(): MaterialJugueteService {
        return retrofit.create(MaterialJugueteService::class.java)
    }
}