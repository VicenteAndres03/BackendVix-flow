package apicalendario.controller;

import apicalendario.dto.CuadernoDto;
import apicalendario.model.Cuaderno;
import apicalendario.service.CuadernoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuadernos")
@AllArgsConstructor
public class CuadernoController {

    private final CuadernoService cuadernoService;

    // Obtener todos los cuadernos del usuario logueado
    @GetMapping
    public ResponseEntity<List<Cuaderno>> obtenerMisCuadernos(Principal principal) {
        return ResponseEntity.ok(cuadernoService.obtenerCuadernosUsuario(principal.getName()));
    }

    // Crear un nuevo cuaderno
    @PostMapping
    public ResponseEntity<?> crearCuaderno(@Valid @RequestBody CuadernoDto dto, Principal principal) {
        try {
            Cuaderno nuevoCuaderno = cuadernoService.crearCuaderno(principal.getName(), dto);
            return new ResponseEntity<>(nuevoCuaderno, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Capturamos el error si el usuario alcanzó el límite Freemium
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Actualizar nombre o descripción del cuaderno
    @PutMapping("/{id}")
    public ResponseEntity<Cuaderno> actualizarCuaderno(@PathVariable Long id, @Valid @RequestBody CuadernoDto dto) {
        return ResponseEntity.ok(cuadernoService.actualizarCuaderno(id, dto));
    }

    // Eliminar un cuaderno (y todas sus hojas)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCuaderno(@PathVariable Long id) {
        return ResponseEntity.ok(cuadernoService.eliminarCuaderno(id));
    }

    @PatchMapping("/{id}/foto")
    public ResponseEntity<Cuaderno> actualizarFotoCuaderno(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Principal principal) {
        String foto = body.get("fotoCuaderno");
        return ResponseEntity.ok(cuadernoService.actualizarFotoCuaderno(id, foto));
    }
}