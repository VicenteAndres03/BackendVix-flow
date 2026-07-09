package apicalendario.controller;

import apicalendario.service.MetricasService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/metricas")
@AllArgsConstructor
public class MetricasController {

    private final MetricasService service;

    @GetMapping("/personales")
    public Map<String, Object> obtenerMetricasPersonales() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerMetricasPersonales(email);
    }

    @GetMapping("/proyectos")
    public Map<String, Object> obtenerMetricasProyectos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerMetricasProyectos(email);
    }

    @PostMapping("/actualizar-racha")
    public void actualizarRacha(@RequestParam LocalDate fecha) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        service.actualizarRacha(email, fecha);
    }
}