package apicalendario.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import apicalendario.dto.TareaProyectoDto;
import apicalendario.model.Estado;
import apicalendario.model.TareaProyecto;
import apicalendario.service.TareaProyectoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tareas-proyecto")
@AllArgsConstructor
public class TareaProyectoController {

    private final TareaProyectoService service;

    @PostMapping("/crear/{proyectoId}")
    public String crearTarea(@PathVariable Long proyectoId, @Valid @RequestBody TareaProyectoDto dto) {
        return service.crearTarea(proyectoId, dto);
    }

    @GetMapping("/proyecto/{proyectoId}")
    public List<TareaProyecto> obtenerTareas(@PathVariable Long proyectoId) {
        return service.obtenerTareasPorProyecto(proyectoId);
    }

    @PatchMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam Estado nuevoEstado) {
        return service.cambiarEstado(id, nuevoEstado);
    }

    @DeleteMapping("/eliminar/{id}")
    public String eliminarTarea(@PathVariable Long id) {
        return service.eliminarTarea(id);
    }
}