package apicalendario.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import apicalendario.dto.AmistadDto;
import apicalendario.model.Amistad;
import apicalendario.service.AmistadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/amigos")
@AllArgsConstructor
public class AmistadController {

    private final AmistadService service;

    @PostMapping("/solicitar")
    public String enviarSolicitud(@Valid @RequestBody AmistadDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.enviarSolicitud(email, dto);
    }

    @PutMapping("/aceptar/{id}")
    public String aceptarSolicitud(@PathVariable Long id) {
        return service.aceptarSolicitud(id);
    }

    @PutMapping("/rechazar/{id}")
    public String rechazarSolicitud(@PathVariable Long id) {
        return service.rechazarSolicitud(id);
    }

    @GetMapping("/solicitudes")
    public List<Amistad> obtenerSolicitudes() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerSolicitudesPendientes(email);
    }

    @GetMapping("/lista")
    public List<Amistad> obtenerAmigos() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.obtenerAmigos(email);
    }

    // 👇 ESTE ES EL NUEVO ENDPOINT PARA ELIMINAR 👇
    @DeleteMapping("/eliminar")
    public String eliminarAmigo(@RequestParam("emailAmigo") String emailAmigo) {
        String emailUsuario = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.eliminarAmigo(emailUsuario, emailAmigo);
    }
}