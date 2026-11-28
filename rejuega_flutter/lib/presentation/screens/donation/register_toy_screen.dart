// (nuevo) Registro de donación con foto (cámara/galería), material y CO₂.
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import 'package:uuid/uuid.dart';
import '../../../data/models/app_models.dart';
import '../../../data/services/cloudinary_service.dart';
import '../../../state/auth_providers.dart';
import '../../../state/campaign_providers.dart';
import '../../../state/data_providers.dart';

class RegisterToyScreen extends ConsumerStatefulWidget {
  final String campaignId;
  final String campaignTitulo;
  const RegisterToyScreen({
    super.key,
    required this.campaignId,
    required this.campaignTitulo,
  });

  @override
  ConsumerState<RegisterToyScreen> createState() => _RegisterToyScreenState();
}

class _RegisterToyScreenState extends ConsumerState<RegisterToyScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nombreCtrl = TextEditingController();
  final _descCtrl = TextEditingController();
  String _condicion = 'Buen Estado';
  String? _materialTipo;
  XFile? _imagen;
  bool _isLoading = false;

  Future<void> _pickImage(ImageSource source) async {
    final picker = ImagePicker();
    final x = await picker.pickImage(
      source: source,
      maxWidth: 1024,
      imageQuality: 80,
    );
    if (x != null) setState(() => _imagen = x);
  }

  Future<void> _guardar() async {
    if (!_formKey.currentState!.validate()) return;
    if (_imagen == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Agrega una foto del juguete')),
      );
      return;
    }
    if (_materialTipo == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Selecciona el material del juguete')),
      );
      return;
    }

    setState(() => _isLoading = true);
    final uid = ref.read(currentUserProvider).value?.uid ?? '';
    final materiales = ref.read(materialsProvider).value ?? [];
    final material = materiales.firstWhere((m) => m.tipoMaterial == _materialTipo);

    try {
      // (nuevo) 1) Sube la foto a Cloudinary.
      final url = await CloudinaryService.subirImagen(File(_imagen!.path));

      // (nuevo) 2) Construye la donación con historial inicial Pendiente.
      final now = DateTime.now().millisecondsSinceEpoch;
      final donacion = DonationModel(
        id: const Uuid().v4(),
        uid: uid,
        campaignFirestoreId: widget.campaignId,
        campaignTitulo: widget.campaignTitulo,
        nombre: _nombreCtrl.text.trim(),
        descripcion: _descCtrl.text.trim(),
        condicion: _condicion,
        fotoUrl: url,
        materialTipo: material.tipoMaterial,
        co2AhorradoKg: material.co2AhorradoKg,
        fechaRegistro: now,
        historial: [
          HistorialEstado(estado: 'Pendiente', fecha: now, cambiadoPor: uid),
        ],
      );

      // (nuevo) 3) Guarda con patrón Node primario / Firestore respaldo.
      await ref.read(donationRepositoryProvider).registrar(donacion);

      // (nuevo) 4) Refresca listas afectadas.
      ref.invalidate(myDonationsProvider);
      ref.invalidate(unreadCountProvider);

      if (!mounted) return;
      context.pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('¡Donación registrada con éxito! 🧸')),
      );
    } catch (e) {
      if (!mounted) return;
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al registrar: $e')),
      );
    }
  }

  @override
  void dispose() {
    _nombreCtrl.dispose();
    _descCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final materialesAsync = ref.watch(materialsProvider);

    return Scaffold(
      appBar: AppBar(title: Text('Donar a ${widget.campaignTitulo}')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // (nuevo) Selector de foto.
            GestureDetector(
              onTap: () => _pickImage(ImageSource.gallery),
              child: Container(
                height: 200,
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: _imagen == null
                    ? const Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.add_a_photo, size: 48),
                          SizedBox(height: 8),
                          Text('Toca para agregar una foto'),
                        ],
                      )
                    : ClipRRect(
                        borderRadius: BorderRadius.circular(16),
                        child: Image.file(File(_imagen!.path), fit: BoxFit.cover),
                      ),
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                TextButton.icon(
                  onPressed: () => _pickImage(ImageSource.camera),
                  icon: const Icon(Icons.camera_alt),
                  label: const Text('Cámara'),
                ),
                TextButton.icon(
                  onPressed: () => _pickImage(ImageSource.gallery),
                  icon: const Icon(Icons.photo_library),
                  label: const Text('Galería'),
                ),
              ],
            ),
            const SizedBox(height: 16),

            TextFormField(
              controller: _nombreCtrl,
              decoration: const InputDecoration(labelText: 'Nombre del juguete'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Obligatorio' : null,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _descCtrl,
              maxLines: 3,
              decoration: const InputDecoration(labelText: 'Descripción'),
            ),
            const SizedBox(height: 16),

            // (nuevo) Condición: Nuevo / Buen Estado.
            Text('Condición', style: Theme.of(context).textTheme.titleSmall),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'Nuevo', label: Text('Nuevo')),
                ButtonSegment(value: 'Buen Estado', label: Text('Buen Estado')),
              ],
              selected: {_condicion},
              onSelectionChanged: (s) => setState(() => _condicion = s.first),
            ),
            const SizedBox(height: 16),

            // (nuevo) Material con su CO₂.
            materialesAsync.when(
              loading: () => const LinearProgressIndicator(),
              error: (e, _) => Text('Error materiales: $e'),
              data: (materiales) => DropdownButtonFormField<String>(
                value: _materialTipo,
                decoration: const InputDecoration(labelText: 'Material principal'),
                items: materiales
                    .map((m) => DropdownMenuItem(
                          value: m.tipoMaterial,
                          child: Text('${m.tipoMaterial} · ${m.co2AhorradoKg} kg CO₂'),
                        ))
                    .toList(),
                onChanged: (v) => setState(() => _materialTipo = v),
              ),
            ),
            const SizedBox(height: 24),

            FilledButton(
              onPressed: _isLoading ? null : _guardar,
              child: _isLoading
                  ? const SizedBox(
                      width: 20, height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                    )
                  : const Text('Registrar donación'),
            ),
          ],
        ),
      ),
    );
  }
}
