// (nuevo) Utilidades de fechas equivalentes a isoAFechaVisible / formatearFechaHora de Kotlin.
class AppDateUtils {
  AppDateUtils._();

  // (nuevo) '2026-11-22' o ISO completo → '22/11/2026'.
  static String fechaVisible(dynamic valor) {
    if (valor == null) return 'Sin fecha';
    final s = valor.toString();
    if (s.isEmpty) return 'Sin fecha';
    final parte = s.length >= 10 ? s.substring(0, 10) : s;
    final partes = parte.split('-');
    if (partes.length != 3) return parte;
    return '${partes[2]}/${partes[1]}/${partes[0]}';
  }

  // (nuevo) Milisegundos → '22/11/2026 21:34'. Evita el bug del 1969.
  static String fechaHora(int? millis) {
    if (millis == null || millis <= 0) return 'Sin fecha';
    final d = DateTime.fromMillisecondsSinceEpoch(millis);
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(d.day)}/${two(d.month)}/${d.year} ${two(d.hour)}:${two(d.minute)}';
  }
}
