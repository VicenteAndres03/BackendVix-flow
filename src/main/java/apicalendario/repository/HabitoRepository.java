package apicalendario.repository;

import apicalendario.model.Habito;
import apicalendario.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitoRepository extends JpaRepository<Habito, Long> {
    List<Habito> findByUsuario(User usuario);
}