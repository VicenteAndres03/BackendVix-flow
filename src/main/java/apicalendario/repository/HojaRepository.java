package apicalendario.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import apicalendario.model.Cuaderno;
import apicalendario.model.Hoja;

public interface HojaRepository extends JpaRepository<Hoja, Long> {

    // Obtener todas las hojas dentro de un cuaderno específico
    List<Hoja> findByCuaderno(Cuaderno cuaderno);

    // Contar cuántas hojas tiene un cuaderno (Para el candado Freemium)
    long countByCuaderno(Cuaderno cuaderno);

    // Para poder borrar todas las hojas cuando se elimina un cuaderno
    void deleteByCuaderno(Cuaderno cuaderno);
}