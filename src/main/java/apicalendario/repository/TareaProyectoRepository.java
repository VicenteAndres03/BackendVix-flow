package apicalendario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.model.Proyecto;
import apicalendario.model.TareaProyecto;
import apicalendario.model.User;

public interface TareaProyectoRepository extends JpaRepository<TareaProyecto, Long> {
    List<TareaProyecto> findByProyecto(Proyecto proyecto);

    List<TareaProyecto> findByAsignadoA(User usuario);

    void deleteByProyecto(Proyecto proyecto);
}