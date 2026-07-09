package apicalendario.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import apicalendario.dto.ActualizarPerfilDto;
import apicalendario.dto.LoginDto;
import apicalendario.dto.LoginResponseDto;
import apicalendario.dto.RegisterDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.EstadoSuscripcion;
import apicalendario.model.User;
import apicalendario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@AllArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping("/registro")
    public String registrarUsuario(@Valid @RequestBody RegisterDto registro) {
        return service.registrarUsuario(registro);
    }

    @PostMapping("/login")
    public LoginResponseDto IniciarSesion(@Valid @RequestBody LoginDto email) {
        return service.IniciarSesion(email);
    }

    @DeleteMapping("/borrar")
    public String borrarUsuario(HttpServletRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String token = request.getHeader("Authorization").substring(7);

        return service.BorrarUsuario(email, token);
    }

    @PutMapping("/modificar")
    public String ModificarUsuario(@Valid @RequestBody ActualizarPerfilDto actualizar) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return service.ModificarUsuario(email, actualizar);
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<User>> obtenerTodos() {
        return ResponseEntity.ok(service.obtenerTodosLosUsuarios());
    }

    @PutMapping("/admin/suscripcion/{id}")
    public ResponseEntity<String> cambiarSuscripcion(@PathVariable Long id, @RequestParam EstadoSuscripcion estado) {
        return ResponseEntity.ok(service.cambiarEstadoSuscripcion(id, estado));
    }

    @DeleteMapping("/admin/eliminar/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(service.eliminarUsuario(id));
    }

    @GetMapping("/perfil")
    public ResponseEntity<LoginResponseDto> obtenerPerfil() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = service.obtenerPorEmail(email);

        // 👇 Agregamos usuario.getApellido() como tercer parámetro 👇
        LoginResponseDto dto = new LoginResponseDto(
                null,
                usuario.getNombre(),
                usuario.getApellido(), // <--- ESTE ES EL NUEVO DATO
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getEstadoSuscripcion().name(),
                usuario.isPruebaConsumida());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/canjear-prueba")
    public ResponseEntity<String> canjearMesGratis() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User usuario = service.obtenerPorEmail(email);

        if (usuario.isPruebaConsumida()) {
            return ResponseEntity.badRequest().body("Ya has canjeado tu mes de prueba gratuito anteriormente.");
        }

        usuario.setEstadoSuscripcion(EstadoSuscripcion.ACTIVO);
        usuario.setPruebaConsumida(true);
        // 👇 AQUÍ ESTÁ LA MAGIA: Le sumamos 30 días exactos a la fecha actual
        usuario.setFechaFinPremium(java.time.LocalDate.now().plusDays(30));

        service.guardarUsuario(usuario);

        return ResponseEntity.ok("¡Felicidades! Tu mes gratis Premium ha sido activado.");
    }

    @PostMapping("/recuperar-password")
    public ResponseEntity<String> recuperarPassword(@RequestParam String email) {
        try {
            String mensaje = service.recuperarPassword(email);
            return ResponseEntity.ok(mensaje);
        } catch (UsuarioNoEncontradoException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ocurrió un error al procesar la solicitud.");
        }
    }

    @PostMapping("/foto-perfil")
    public ResponseEntity<String> actualizarFotoPerfil(
            @RequestBody Map<String, String> body) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String foto = body.get("fotoPerfil");
        service.actualizarFotoPerfil(email, foto);
        return ResponseEntity.ok("Foto actualizada");
    }

    @GetMapping("/foto-perfil/{email}")
    public ResponseEntity<Map<String, String>> obtenerFotoPerfil(
            @PathVariable String email) {
        String foto = service.obtenerFotoPerfil(email);
        Map<String, String> response = new HashMap<>();
        response.put("fotoPerfil", foto != null ? foto : "");
        return ResponseEntity.ok(response);
    }
}
