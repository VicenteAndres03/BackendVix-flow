package apicalendario.service;

import org.springframework.stereotype.Service;

import apicalendario.model.TokenBlacklist;
import apicalendario.repository.TokenBlacklistRepository;
import apicalendario.security.JwtService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TokenBlacklistService {

    private final TokenBlacklistRepository repositorio;
    private final JwtService jwtService;

    public String agregarToken(String token) {

        TokenBlacklist tok = TokenBlacklist.builder()
                .token(token)
                .fechaExpiracion(jwtService.extraerFechaExpiracion(token))
                .build();
        repositorio.save(tok);
        return "Token agregado de manera correcta";
    }

    public boolean estaEnBlackList(String token) {
        return repositorio.existsByToken(token);
    }

}
