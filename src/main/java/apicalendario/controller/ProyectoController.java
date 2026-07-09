package apicalendario.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import apicalendario.dto.ProyectoDto;
import apicalendario.model.ProyectoMiembro;
import apicalendario.service.ProyectoService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/proyectos")
@AllArgsConstructor
public class ProyectoController {

    private final ProyectoService service;

    @PostMapping("/crear")
    public String crearProyecto(@Valid @RequestBody ProyectoDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.crearProyecto(email, dto);
    }

    @PostMapping("/invitar/{proyectoId}")
    public String invitarMiembro(@PathVariable Long proyectoId, @RequestParam String emailInvitado) {
        return service.invitarMiembro(proyectoId, emailInvitado);
    }

    @GetMapping("/mis")
    public List<ProyectoMiembro> obtenerMisProyectos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerMisProyectos(email);
    }

    // --- NUEVOS ENDPOINTS ---

    @DeleteMapping("/eliminar/{proyectoId}")
    public String eliminarProyecto(@PathVariable Long proyectoId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.eliminarProyecto(proyectoId, email);
    }

    @GetMapping("/progreso/{proyectoId}")
    public int obtenerProgreso(@PathVariable Long proyectoId) {
        return service.obtenerProgreso(proyectoId);
    }

    @PostMapping("/aceptar-invitacion/{token}")
    public String aceptarInvitacion(@PathVariable String token) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.aceptarInvitacion(token, email);
    }

    @GetMapping("/miembros/{proyectoId}")
    public List<ProyectoMiembro> obtenerMiembrosProyecto(@PathVariable Long proyectoId) {
        return service.obtenerMiembrosPorProyecto(proyectoId);
    }
}