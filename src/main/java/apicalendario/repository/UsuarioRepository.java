package apicalendario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.model.User;

public interface UsuarioRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}
