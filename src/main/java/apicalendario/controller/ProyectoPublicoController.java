package apicalendario.controller;

import apicalendario.repository.ProyectoRepository;
import apicalendario.repository.TareaProyectoRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/proyectos")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ProyectoPublicoController {

    private final ProyectoRepository proyectoRepo;
    private final TareaProyectoRepository tareaProyectoRepo;

    @GetMapping("/{tokenPublico}")
    public ResponseEntity<Map<String, Object>> obtenerProyectoPublico(@PathVariable String tokenPublico) {
        var proyecto = proyectoRepo.findByTokenPublico(tokenPublico)
                .orElseThrow(() -> new RuntimeException("Enlace no válido o expirado"));

        var tareas = tareaProyectoRepo.findByProyecto(proyecto);

        Map<String, Object> response = new HashMap<>();
        response.put("nombreProyecto", proyecto.getNombre());
        response.put("tareas", tareas);

        return ResponseEntity.ok(response);
    }
}