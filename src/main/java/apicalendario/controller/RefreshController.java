package apicalendario.controller;

import apicalendario.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RefreshController {

    @Autowired
    private JwtService jwtService;

    // POST /api/auth/refresh
    // Header: Authorization: Bearer <token_viejo>
    // Devuelve: { "token": "nuevo_token" }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("mensaje", "Token no proporcionado"));
            }

            String tokenViejo = authHeader.substring(7);
            String email = jwtService.extraerEmail(tokenViejo);

            if (email == null || email.isBlank()) {
                return ResponseEntity.status(401).body(Map.of("mensaje", "Token inválido"));
            }

            // Generar nuevo token con el mismo email
            String nuevoToken = jwtService.generarToken(email);
            return ResponseEntity.ok(Map.of("token", nuevoToken));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("mensaje", "Token expirado o inválido"));
        }
    }
}
