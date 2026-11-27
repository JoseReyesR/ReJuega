// (nuevo) Parsers defensivos: Node puede mandar números, strings ISO o Dates.
// (nuevo) Equivalente al aMillis() robusto del servidor Kotlin/Node.

// (nuevo) Convierte cualquier formato de fecha a milisegundos.
int? parseMillis(dynamic v) {
  if (v == null) return null;
  if (v is num) return v.toInt();
  if (v is String) {
    final t = DateTime.tryParse(v.replaceAll(' ', 'T'));
    return t?.millisecondsSinceEpoch;
  }
  return null;
}

// (nuevo) Normaliza fechas DATE a 'YYYY-MM-DD'.
String parseFechaDate(dynamic v) {
  if (v == null) return '';
  final s = v.toString();
  return s.length >= 10 ? s.substring(0, 10) : s;
}

double parseDouble(dynamic v) =>
    v is num ? v.toDouble() : (double.tryParse(v?.toString() ?? '') ?? 0.0);

int parseInt(dynamic v) =>
    v is num ? v.toInt() : (int.tryParse(v?.toString() ?? '') ?? 0);

bool parseBool(dynamic v) {
  if (v is bool) return v;
  if (v is num) return v != 0;
  return v?.toString().toLowerCase() == 'true';
}

String parseStr(dynamic v) => v?.toString() ?? '';

// (nuevo) Lista desde array (Firestore) o string 'Lun,Mar' (MySQL).
List<String> parseList(dynamic v) {
  if (v is List) return v.map((e) => e.toString()).toList();
  if (v is String && v.isNotEmpty) return v.split(',');
  return [];
}
