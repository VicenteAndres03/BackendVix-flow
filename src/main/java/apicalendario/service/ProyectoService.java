package apicalendario.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import apicalendario.dto.ProyectoDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.Estado;
import apicalendario.model.EstadoInvitacion;
import apicalendario.model.Proyecto;
import apicalendario.model.ProyectoMiembro;
import apicalendario.model.RolProyecto;
import apicalendario.model.TareaProyecto;
import apicalendario.model.User;
import apicalendario.repository.ProyectoMiembroRepository;
import apicalendario.repository.ProyectoRepository;
import apicalendario.repository.TareaProyectoRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProyectoService {

        private final ProyectoRepository repositorio;
        private final ProyectoMiembroRepository repositorioMiembro;
        private final UsuarioRepository repositorioU;
        private final TareaProyectoRepository tareaProyectoRepo;
        private final EmailService emailService;

        public String crearProyecto(String email, ProyectoDto dto) {
                User creador = repositorioU.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

                Proyecto proyecto = Proyecto.builder()
                                .nombre(dto.getNombre())
                                .descripcion(dto.getDescripcion())
                                .creador(creador)
                                .build();
                repositorio.save(proyecto);

                ProyectoMiembro miembro = ProyectoMiembro.builder()
                                .proyecto(proyecto)
                                .usuario(creador)
                                .rol(RolProyecto.ADMIN)
                                .estado(EstadoInvitacion.ACEPTADO) // El creador entra directamente sin invitación
                                .build();
                repositorioMiembro.save(miembro);

                return "Proyecto creado correctamente";
        }

        public String invitarMiembro(Long proyectoId, String emailInvitado) {
                Proyecto proyecto = repositorio.findById(proyectoId)
                                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
                User invitado = repositorioU.findByEmail(emailInvitado)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

                if (repositorioMiembro.existsByProyectoAndUsuario(proyecto, invitado)) {
                        return "El usuario ya es miembro o tiene una invitación pendiente";
                }

                // Generamos un token único de 36 caracteres para la URL de la invitación
                String token = UUID.randomUUID().toString();

                ProyectoMiembro miembro = ProyectoMiembro.builder()
                                .proyecto(proyecto)
                                .usuario(invitado)
                                .rol(RolProyecto.MIEMBRO)
                                // Nota: El estado se establece en PENDIENTE por defecto desde el modelo
                                // (Builder.Default)
                                .tokenInvitacion(token)
                                .build();
                repositorioMiembro.save(miembro);

                // Enviamos el correo de invitación usando el token
                emailService.enviarCorreoInvitacionProyecto(emailInvitado, proyecto.getNombre(), token);

                return "Invitación enviada por correo correctamente";
        }

        public String aceptarInvitacion(String token, String emailLogueado) {
                ProyectoMiembro miembro = repositorioMiembro.findByTokenInvitacion(token)
                                .orElseThrow(() -> new RuntimeException("Invitación no válida o expirada"));

                // Verificación de seguridad: solo el dueño del correo puede aceptarla
                if (!miembro.getUsuario().getEmail().equals(emailLogueado)) {
                        throw new RuntimeException("Esta invitación no pertenece a tu cuenta.");
                }

                if (miembro.getEstado() == EstadoInvitacion.ACEPTADO) {
                        return "Esta invitación ya fue aceptada";
                }

                miembro.setEstado(EstadoInvitacion.ACEPTADO);
                miembro.setTokenInvitacion(null); // Limpiamos el token por seguridad, ya no se usa
                repositorioMiembro.save(miembro);

                return "Invitación aceptada exitosamente";
        }

        public List<ProyectoMiembro> obtenerMisProyectos(String email) {
                User usuario = repositorioU.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
                // Filtramos para mostrar únicamente los proyectos donde el usuario ha ACEPTADO
                return repositorioMiembro.findByUsuarioAndEstado(usuario, EstadoInvitacion.ACEPTADO);
        }

        @Transactional // Fundamental: Evita errores de consistencia al borrar en cascada
        public String eliminarProyecto(Long proyectoId, String email) {
                User usuario = repositorioU.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
                Proyecto proyecto = repositorio.findById(proyectoId)
                                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

                // Validamos si es administrador del proyecto
                ProyectoMiembro miembro = repositorioMiembro.findByProyecto(proyecto).stream()
                                .filter(m -> m.getUsuario().getId().equals(usuario.getId()))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("No eres miembro de este proyecto"));

                if (miembro.getRol() != RolProyecto.ADMIN) {
                        throw new RuntimeException("Solo el administrador puede eliminar el proyecto");
                }

                // 1. Eliminamos todas las tareas asociadas al proyecto
                tareaProyectoRepo.deleteByProyecto(proyecto);
                // 2. Eliminamos a todos los miembros (y sus invitaciones pendientes) del
                // proyecto
                repositorioMiembro.deleteByProyecto(proyecto);
                // 3. Finalmente, eliminamos el proyecto
                repositorio.delete(proyecto);

                return "Proyecto eliminado exitosamente";
        }

        public int obtenerProgreso(Long proyectoId) {
                Proyecto proyecto = repositorio.findById(proyectoId)
                                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

                List<TareaProyecto> tareas = tareaProyectoRepo.findByProyecto(proyecto);
                if (tareas.isEmpty()) {
                        return 0; // Si no hay tareas creadas, el progreso es 0%
                }

                long completadas = tareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO)
                                .count();

                // Regla de 3 simple para el porcentaje (completadas / totales * 100)
                return (int) ((completadas * 100) / tareas.size());
        }

        public List<ProyectoMiembro> obtenerMiembros(Long proyectoId) {
                Proyecto proyecto = repositorio.findById(proyectoId)
                                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
                return repositorioMiembro.findByProyecto(proyecto);
        }

        public List<ProyectoMiembro> obtenerMiembrosPorProyecto(Long proyectoId) {
                Proyecto proyecto = repositorio.findById(proyectoId)
                                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

                // 🔥 CORRECCIÓN: Ahora busca en el repositorio correcto (repositorioMiembro)
                return repositorioMiembro.findByProyecto(proyecto);
        }

}