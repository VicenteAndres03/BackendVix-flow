package apicalendario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import apicalendario.model.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

}
