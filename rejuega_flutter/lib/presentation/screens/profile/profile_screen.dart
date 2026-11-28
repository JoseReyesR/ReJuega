// (nuevo) Mi Perfil: datos editables + rol de solo lectura + cambio de contraseña.
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_colors.dart';
import '../../../data/models/user_model.dart';
import '../../../state/auth_providers.dart';

class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nombreCtrl = TextEditingController();
  final _telefonoCtrl = TextEditingController();
  final _ciudadCtrl = TextEditingController();
  final _direccionCtrl = TextEditingController();
  String _genero = '';
  bool _isLoading = false;
  bool _prefilled = false;

  @override
  void initState() {
    super.initState();
    // (nuevo) Prefill cuando el perfil ya está disponible.
    WidgetsBinding.instance.addPostFrameCallback((_) => _prefill());
  }

  void _prefill() {
    if (_prefilled) return;
    final p = ref.read(currentUserProvider).value;
    if (p == null) return;
    setState(() {
      _nombreCtrl.text = p.nombre;
      _telefonoCtrl.text = p.telefono;
      _ciudadCtrl.text = p.ciudad;
      _direccionCtrl.text = p.direccion;
      _genero = p.genero;
      _prefilled = true;
    });
  }

  Future<void> _guardar() async {
    if (!_formKey.currentState!.validate()) return;
    final actual = ref.read(currentUserProvider).value;
    if (actual == null) return;

    setState(() => _isLoading = true);

    final actualizado = UserModel(
      uid: actual.uid,
      nombre: _nombreCtrl.text.trim(),
      correo: actual.correo,
      rol: actual.rol,
      direccion: _direccionCtrl.text.trim(),
      telefono: _telefonoCtrl.text.trim(),
      ciudad: _ciudadCtrl.text.trim(),
      genero: _genero,
    );

    try {
      await ref.read(authServiceProvider).actualizarPerfil(actualizado);
      if (!mounted) return;
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Perfil actualizado ✅')),
      );
    } catch (e) {
      if (!mounted) return;
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al guardar: $e')),
      );
    }
  }

  // (nuevo) Cambio de contraseña vía email de Firebase Auth (igual que Kotlin).
  Future<void> _cambiarContrasena() async {
    final correo = ref.read(currentUserProvider).value?.correo;
    if (correo == null || correo.isEmpty) return;
    try {
      await FirebaseAuth.instance.sendPasswordResetEmail(email: correo);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Te enviamos un correo a $correo para restablecer tu contraseña.')),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al enviar correo: $e')),
      );
    }
  }

  @override
  void dispose() {
    _nombreCtrl.dispose();
    _telefonoCtrl.dispose();
    _ciudadCtrl.dispose();
    _direccionCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final perfilAsync = ref.watch(currentUserProvider);
    final perfil = perfilAsync.value;

    // (nuevo) Si el perfil llega después del primer frame, prefill reactivo.
    if (perfil != null && !_prefilled) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _prefill());
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Mi Perfil')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // (nuevo) Cabecera con rol.
            Row(
              children: [
                CircleAvatar(
                  radius: 28,
                  child: Text(
                    (perfil?.nombre.isNotEmpty == true)
                        ? perfil!.nombre[0].toUpperCase()
                        : '?',
                  ),
                ),
                const SizedBox(width: 12),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(perfil?.correo ?? '',
                        style: Theme.of(context).textTheme.bodySmall),
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: AppColors.colorForRol(perfil?.rol ?? 'Donante'),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text(perfil?.rol ?? 'Donante',
                          style: const TextStyle(
                              color: Colors.white, fontSize: 12)),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 24),

            TextFormField(
              controller: _nombreCtrl,
              decoration: const InputDecoration(labelText: 'Nombre completo'),
              validator: (v) =>
                  (v == null || v.trim().length < 3) ? 'Ingresa tu nombre' : null,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _telefonoCtrl,
              keyboardType: TextInputType.phone,
              decoration: const InputDecoration(labelText: 'Teléfono'),
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _ciudadCtrl,
              decoration: const InputDecoration(labelText: 'Ciudad'),
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _direccionCtrl,
              decoration: const InputDecoration(labelText: 'Dirección'),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _genero.isEmpty ? null : _genero,
              decoration: const InputDecoration(labelText: 'Género'),
              items: const [
                DropdownMenuItem(value: 'Femenino', child: Text('Femenino')),
                DropdownMenuItem(value: 'Masculino', child: Text('Masculino')),
                DropdownMenuItem(value: 'Otro', child: Text('Otro')),
                DropdownMenuItem(
                    value: 'Prefiero no decir',
                    child: Text('Prefiero no decir')),
              ],
              onChanged: (v) => setState(() => _genero = v ?? ''),
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
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: _cambiarContrasena,
              icon: const Icon(Icons.lock_reset),
              label: const Text('Cambiar contraseña'),
            ),
          ],
        ),
      ),
    );
  }
}
