package apicalendario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import apicalendario.model.Amistad;
import apicalendario.model.EstadoAmistad;
import apicalendario.model.User;

public interface AmistadRepository extends JpaRepository<Amistad, Long> {

    List<Amistad> findByReceptorAndEstado(User receptor, EstadoAmistad estado);

    List<Amistad> findBySolicitanteAndEstado(User solicitante, EstadoAmistad estado);

    Optional<Amistad> findBySolicitanteAndReceptor(User solicitante, User receptor);

    boolean existsBySolicitanteAndReceptor(User solicitante, User receptor);

    List<Amistad> findBySolicitanteOrReceptor(User solicitante, User receptor);

    // 🔥 NUEVA CONSULTA: Busca amigos sin importar quién envió la solicitud 🔥
    @Query("SELECT a FROM Amistad a WHERE (a.solicitante = :usuario OR a.receptor = :usuario) AND a.estado = :estado")
    List<Amistad> findAmigosConfirmados(@Param("usuario") User usuario, @Param("estado") EstadoAmistad estado);
}