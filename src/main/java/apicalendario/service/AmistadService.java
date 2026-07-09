package apicalendario.service;

import java.util.List;

import org.springframework.stereotype.Service;

import apicalendario.dto.AmistadDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.Amistad;
import apicalendario.model.EstadoAmistad;
import apicalendario.model.User;
import apicalendario.repository.AmistadRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AmistadService {

    private final AmistadRepository repositorio;
    private final UsuarioRepository repositorioU;

    public String enviarSolicitud(String emailSolicitante, AmistadDto dto) {
        User solicitante = repositorioU.findByEmail(emailSolicitante)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        User receptor = repositorioU.findByEmail(dto.getEmailReceptor())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario receptor no encontrado"));

        if (repositorio.existsBySolicitanteAndReceptor(solicitante, receptor) ||
                repositorio.existsBySolicitanteAndReceptor(receptor, solicitante)) {
            return "Ya existe una solicitud pendiente o amistad con este usuario";
        }

        Amistad amistad = Amistad.builder()
                .solicitante(solicitante)
                .receptor(receptor)
                .estado(EstadoAmistad.PENDIENTE) // Aseguramos que inicie en pendiente
                .build();
        repositorio.save(amistad);
        return "Solicitud enviada correctamente";
    }

    public String aceptarSolicitud(Long id) {
        Amistad amistad = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        amistad.setEstado(EstadoAmistad.ACEPTADA);
        repositorio.save(amistad);
        return "Solicitud aceptada";
    }

    public String rechazarSolicitud(Long id) {
        Amistad amistad = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        amistad.setEstado(EstadoAmistad.RECHAZADA);
        repositorio.save(amistad);
        return "Solicitud rechazada";
    }

    public List<Amistad> obtenerSolicitudesPendientes(String email) {
        User usuario = repositorioU.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return repositorio.findByReceptorAndEstado(usuario, EstadoAmistad.PENDIENTE);
    }

    public List<Amistad> obtenerAmigos(String email) {
        User usuario = repositorioU.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 🔥 CORRECCIÓN: Ahora busca en ambas direcciones usando el Query personalizado
        // 🔥
        return repositorio.findAmigosConfirmados(usuario, EstadoAmistad.ACEPTADA);
    }

    // Método para eliminar amigos que habíamos creado antes
    public String eliminarAmigo(String emailUsuario, String emailAmigo) {
        User usuario = repositorioU.findByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        User amigo = repositorioU.findByEmail(emailAmigo)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Amigo no encontrado"));

        Amistad amistad = repositorio.findBySolicitanteAndReceptor(usuario, amigo)
                .orElseGet(() -> repositorio.findBySolicitanteAndReceptor(amigo, usuario)
                        .orElseThrow(() -> new RuntimeException("No existe una relación de amistad con este usuario")));

        repositorio.delete(amistad);

        return "Amigo eliminado correctamente";
    }
}