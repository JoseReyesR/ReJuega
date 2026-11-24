// (nuevo) Servicio FCM: guarda el token del dispositivo en users/{uid}.
// (nuevo) Este token lo usará el servidor Node (Paso 20) para enviar push.
package pe.rejuega.app.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ReJuegaFirebaseMessagingService : FirebaseMessagingService() {

    // (nuevo) Se llama cuando Firebase asigna o renueva el token del dispositivo.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("fcmToken", token)
    }

    // (nuevo) Se llama cuando llega un mensaje en foreground (opcional).
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // (nuevo) En foreground la app muestra el mensaje como notificación local
        // (nuevo) en el Paso 17. Por ahora solo log.
        println("FCM recibido: ${message.notification?.title} - ${message.notification?.body}")
    }
}