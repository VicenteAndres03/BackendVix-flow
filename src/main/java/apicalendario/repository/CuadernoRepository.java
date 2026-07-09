package apicalendario.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import apicalendario.model.Cuaderno;
import apicalendario.model.User;

public interface CuadernoRepository extends JpaRepository<Cuaderno, Long> {

    // Obtener todos los cuadernos de un usuario
    List<Cuaderno> findByUsuario(User usuario);

    // Contar cuántos cuadernos tiene un usuario (Para el candado Freemium)
    long countByUsuario(User usuario);
}