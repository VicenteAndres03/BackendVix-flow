package apicalendario.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.model.Tarea;
import apicalendario.model.User;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    List<Tarea> findByUsuario(User usuario);

    List<Tarea> findByUsuarioAndFechaInicioBetween(User usuario, LocalDateTime inicio, LocalDateTime fin);

    List<Tarea> findByUsuarioEmail(String email);

}
