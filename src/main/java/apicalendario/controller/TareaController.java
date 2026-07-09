package apicalendario.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import apicalendario.dto.TareaDto;
import apicalendario.model.Estado;
import apicalendario.model.Tarea;
import apicalendario.service.TareaService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/tarea")
@AllArgsConstructor
public class TareaController {

    private final TareaService service;

    @PostMapping("/guardar")
    public String RegistrarTarea(@Valid @RequestBody TareaDto tarea) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.RegistrarTarea(email, tarea);
    }

    @GetMapping("/dia")
    public List<Tarea> TareadelDia(@RequestParam LocalDate fecha) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.TareadelDia(email, fecha);
    }

    // NUEVO ENDPOINT PARA LAS FIGURITAS DEL CALENDARIO
    @GetMapping("/todas")
    public List<Tarea> obtenerTodasLasTareas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerTodasLasTareas(email);
    }

    @DeleteMapping("/{id}")
    public String eliminarTarea(@PathVariable Long id) {
        return service.eliminarTarea(id);
    }

    @PutMapping("/{id}")
    public String actualizarTarea(@PathVariable Long id, @Valid @RequestBody TareaDto tarea) {
        return service.actualizarTarea(id, tarea);
    }

    @PatchMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam Estado nuevoEstado,
            @RequestParam(required = false) LocalDate fecha) {
        return service.cambiarEstado(id, nuevoEstado, fecha);
    }
}