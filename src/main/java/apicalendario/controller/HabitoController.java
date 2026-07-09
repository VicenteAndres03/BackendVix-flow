package apicalendario.controller;

import apicalendario.model.Habito;
import apicalendario.service.HabitoService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habitos")
@AllArgsConstructor
public class HabitoController {

    private final HabitoService habitoService;

    @GetMapping
    public List<Habito> obtenerHabitos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return habitoService.obtenerHabitosPorUsuario(email);
    }

    @PostMapping
    public Habito crearHabito(@RequestBody Map<String, String> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return habitoService.crearHabito(email, payload.get("nombre"));
    }

    @DeleteMapping("/{id}")
    public void eliminarHabito(@PathVariable Long id) {
        habitoService.eliminarHabito(id);
    }

    @PatchMapping("/{id}/toggle")
    public Habito toggleFecha(@PathVariable Long id, @RequestParam String fecha) {
        return habitoService.toggleFecha(id, fecha);
    }
}