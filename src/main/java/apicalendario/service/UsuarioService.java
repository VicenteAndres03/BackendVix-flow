package apicalendario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apicalendario.dto.ActualizarPerfilDto;
import apicalendario.dto.LoginDto;
import apicalendario.dto.LoginResponseDto;
import apicalendario.dto.RegisterDto;
import apicalendario.exception.CredencialesInvalidasException;
import apicalendario.exception.CuentaInactivaException;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.*;
import apicalendario.repository.*;
import apicalendario.security.JwtService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenblack;
    private final EmailService emailService;

    // ─────────────────────────────────────────
    // REPOSITORIOS INYECTADOS PARA BORRAR EN CASCADA
    // ─────────────────────────────────────────
    private final TareaRepository tareaRepo;
    private final AmistadRepository amistadRepo;
    private final ProyectoMiembroRepository proyectoMiembroRepo;
    private final TareaProyectoRepository tareaProyectoRepo;

    public String registrarUsuario(RegisterDto registro) {
        Optional<User> usuarioExistente = repositorio.findByEmail(registro.getEmail());
        if (usuarioExistente.isPresent()) {
            User verifi = usuarioExistente.get();
            if (!verifi.isActivo()) {
                String passwordE = passwordEncoder.encode(registro.getPassword());
                verifi.setActivo(true);
                verifi.setNombre(registro.getNombre());
                verifi.setApellido(registro.getApellido());
                verifi.setPassword(passwordE);
                verifi.setEstadoSuscripcion(EstadoSuscripcion.INACTIVO);
                verifi.setRol(Rol.USER);
                repositorio.save(verifi);

                // Envío de correo al reactivar cuenta
                emailService.enviarCorreoBienvenida(registro.getEmail(), registro.getNombre());

                return "El usuario eliminado fue nuevamente registrado de manera correcta";
            } else {
                return "el usuario ya esta registrado";
            }
        } else {
            String passwordE = passwordEncoder.encode(registro.getPassword());

            User usuario = User.builder()
                    .email(registro.getEmail())
                    .nombre(registro.getNombre())
                    .apellido(registro.getApellido())
                    .password(passwordE)
                    .rol(Rol.USER)
                    .estadoSuscripcion(EstadoSuscripcion.INACTIVO)
                    .activo(true)
                    .build();
            repositorio.save(usuario);

            // Envío de correo para un nuevo registro exitoso
            emailService.enviarCorreoBienvenida(registro.getEmail(), registro.getNombre());

            return "El usuario se registro de una manera correcta";
        }
    }

    public LoginResponseDto IniciarSesion(LoginDto email) {
        User usuario = repositorio.findByEmail(email.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (passwordEncoder.matches(email.getPassword(), usuario.getPassword()) && usuario.isActivo()) {
            String token = jwtService.generarToken(usuario.getEmail());

            // 👇 Agregamos usuario.getApellido() como tercer parámetro 👇
            return new LoginResponseDto(
                    token,
                    usuario.getNombre(),
                    usuario.getApellido(), // <--- ESTE ES EL NUEVO DATO
                    usuario.getEmail(),
                    usuario.getRol().name(),
                    usuario.getEstadoSuscripcion().name(),
                    usuario.isPruebaConsumida());
        } else if (passwordEncoder.matches(email.getPassword(), usuario.getPassword()) && !usuario.isActivo()) {
            throw new CuentaInactivaException("Este usuario ha sido eliminado");
        } else {
            throw new CredencialesInvalidasException("Email o contraseña incorrecta");
        }
    }

    public String BorrarUsuario(String email, String token) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no ecnontrado"));
        usuario.setActivo(false);
        repositorio.save(usuario);
        tokenblack.agregarToken(token);
        return "cuenta eliminada correctamente";
    }

    public String ModificarUsuario(String email, ActualizarPerfilDto actualizar) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        String passwordE = passwordEncoder.encode(actualizar.getPassword());
        usuario.setNombre(actualizar.getNombre());
        usuario.setApellido(actualizar.getApellido());
        usuario.setPassword(passwordE);

        repositorio.save(usuario);

        return "usuario modificado de manera exitosa";
    }

    // ─────────────────────────────────────────
    // FUNCIONES DE ADMINISTRADOR
    // ─────────────────────────────────────────
    public List<User> obtenerTodosLosUsuarios() {
        return repositorio.findAll();
    }

    public String cambiarEstadoSuscripcion(Long id, EstadoSuscripcion nuevoEstado) {
        User usuario = repositorio.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        usuario.setEstadoSuscripcion(nuevoEstado);
        repositorio.save(usuario);
        return "Suscripción actualizada a " + nuevoEstado.name();
    }

    @Transactional
    public String eliminarUsuario(Long id) {
        User usuario = repositorio.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 1. Eliminar todas sus tareas personales
        List<Tarea> tareas = tareaRepo.findByUsuario(usuario);
        tareaRepo.deleteAll(tareas);

        // 2. Eliminar todas sus amistades (ya sea que él invitó o lo invitaron)
        List<Amistad> amistades = amistadRepo.findBySolicitanteOrReceptor(usuario, usuario);
        amistadRepo.deleteAll(amistades);

        // 3. Eliminar sus participaciones en proyectos colaborativos
        List<ProyectoMiembro> membresias = proyectoMiembroRepo.findByUsuario(usuario);
        proyectoMiembroRepo.deleteAll(membresias);

        // 4. Desasignarlo de las tareas de proyectos para no romper el proyecto de
        // otros
        List<TareaProyecto> tareasAsignadas = tareaProyectoRepo.findByAsignadoA(usuario);
        for (TareaProyecto tp : tareasAsignadas) {
            tp.setAsignadoA(null);
            tareaProyectoRepo.save(tp);
        }

        // 5. Ahora sí, la base de datos nos dejará eliminar al usuario de forma segura
        repositorio.delete(usuario);

        return "Usuario y todos sus datos asociados fueron eliminados correctamente";
    }

    public User obtenerPorEmail(String email) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 👇 EL COBRADOR: Revisamos si su plan ya venció 👇
        if (usuario.getEstadoSuscripcion() == EstadoSuscripcion.ACTIVO &&
                usuario.getFechaFinPremium() != null &&
                java.time.LocalDate.now().isAfter(usuario.getFechaFinPremium())) {

            // Se le acabó el tiempo, lo devolvemos a INACTIVO
            usuario.setEstadoSuscripcion(EstadoSuscripcion.INACTIVO);
            usuario.setFechaFinPremium(null);
            repositorio.save(usuario);
        }

        return usuario;
    }

    // ─────────────────────────────────────────
    // NUEVO MÉTODO PARA GUARDAR ACTUALIZACIONES DEL USUARIO
    // ─────────────────────────────────────────
    public void guardarUsuario(User usuario) {
        repositorio.save(usuario);
    }

    public String recuperarPassword(String email) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(
                        () -> new UsuarioNoEncontradoException("No existe ninguna cuenta asociada a este correo."));

        // Generamos una contraseña temporal aleatoria de 8 caracteres
        String nuevaPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

        // La encriptamos y la guardamos en la base de datos
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        repositorio.save(usuario);

        // Enviamos el correo
        emailService.enviarCorreoRecuperacion(email, nuevaPassword);

        return "Te hemos enviado un correo con una contraseña temporal.";
    }

    public void actualizarFotoPerfil(String email, String foto) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        usuario.setFotoPerfil(foto);
        repositorio.save(usuario);
    }

    public String obtenerFotoPerfil(String email) {
        User usuario = repositorio.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return usuario.getFotoPerfil();
    }
}