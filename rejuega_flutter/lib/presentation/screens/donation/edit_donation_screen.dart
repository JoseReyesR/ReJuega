// (nuevo) Edición de donación Pendiente (texto, condición y material).
// (nuevo) La foto y el estado de trazabilidad se conservan.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../data/models/app_models.dart';
import '../../../state/campaign_providers.dart';
import '../../../state/data_providers.dart';

class EditDonationScreen extends ConsumerStatefulWidget {
  final DonationModel donacion;
  const EditDonationScreen({super.key, required this.donacion});

  @override
  ConsumerState<EditDonationScreen> createState() => _EditDonationScreenState();
}

class _EditDonationScreenState extends ConsumerState<EditDonationScreen> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _nombreCtrl =
      TextEditingController(text: widget.donacion.nombre);
  late final TextEditingController _descCtrl =
      TextEditingController(text: widget.donacion.descripcion);
  late String _condicion = widget.donacion.condicion;
  late String? _materialTipo = widget.donacion.materialTipo;
  bool _isLoading = false;

  Future<void> _guardar() async {
    if (!_formKey.currentState!.validate()) return;
    if (_materialTipo == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Selecciona el material')),
      );
      return;
    }

    setState(() => _isLoading = true);
    final materiales = ref.read(materialsProvider).value ?? [];
    final material =
        materiales.firstWhere((m) => m.tipoMaterial == _materialTipo);

    // (nuevo) Reconstruye conservando id, foto, estado y historial.
    final actualizada = DonationModel(
      id: widget.donacion.id,
      uid: widget.donacion.uid,
      campaignFirestoreId: widget.donacion.campaignFirestoreId,
      campaignTitulo: widget.donacion.campaignTitulo,
      nombre: _nombreCtrl.text.trim(),
      descripcion: _descCtrl.text.trim(),
      condicion: _condicion,
      fotoUrl: widget.donacion.fotoUrl,
      estadoTrazabilidad: widget.donacion.estadoTrazabilidad,
      motivoRechazo: widget.donacion.motivoRechazo,
      materialTipo: material.tipoMaterial,
      co2AhorradoKg: material.co2AhorradoKg,
      fechaRegistro: widget.donacion.fechaRegistro,
      historial: widget.donacion.historial,
    );

    try {
      await ref.read(donationRepositoryProvider).editar(actualizada);
      ref.invalidate(myDonationsProvider);
      if (!mounted) return;
      context.pop();
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Donación actualizada ✅')),
      );
    } catch (e) {
      if (!mounted) return;
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al actualizar: $e')),
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
      appBar: AppBar(title: const Text('Editar donación')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
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
                      child: CircularProgressIndicator(
                          strokeWidth: 2, color: Colors.white),
                    )
                  : const Text('Guardar cambios'),
            ),
          ],
        ),
      ),
    );
  }
}
