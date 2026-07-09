package apicalendario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.model.EstadoInvitacion;
import apicalendario.model.Proyecto;
import apicalendario.model.ProyectoMiembro;
import apicalendario.model.User;

public interface ProyectoMiembroRepository extends JpaRepository<ProyectoMiembro, Long> {
    List<ProyectoMiembro> findByUsuario(User usuario);

    List<ProyectoMiembro> findByProyecto(Proyecto proyecto);

    boolean existsByProyectoAndUsuario(Proyecto proyecto, User usuario);

    // Agrega esta línea para poder eliminar los miembros asociados al proyecto
    void deleteByProyecto(Proyecto proyecto);

    Optional<ProyectoMiembro> findByTokenInvitacion(String token);

    List<ProyectoMiembro> findByUsuarioAndEstado(User usuario, EstadoInvitacion estado);
}