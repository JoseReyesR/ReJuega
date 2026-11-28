// (nuevo) Subida de imágenes a Cloudinary (equivalente a ImageUtils + Retrofit de Kotlin).
import 'dart:io';
import 'package:dio/dio.dart';
import '../../core/constants/api_config.dart';

class CloudinaryService {
  // (nuevo) Sube un archivo y devuelve la URL pública (secure_url).
  static Future<String> subirImagen(File file) async {
    final formData = FormData.fromMap({
      'file': await MultipartFile.fromFile(file.path),
      'upload_preset': ApiConfig.uploadPreset,
    });

    final resp = await Dio().post(
      'https://api.cloudinary.com/v1_1/${ApiConfig.cloudName}/image/upload',
      data: formData,
    );

    final url = resp.data['secure_url'];
    if (url == null) throw Exception('Cloudinary no devolvió URL');
    return url as String;
  }
}
