package apicalendario.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apicalendario.service.EmailService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/soporte")
@AllArgsConstructor
public class ContactoController {

    private final EmailService emailService;

    @PostMapping("/enviar")
    public ResponseEntity<String> enviarMensaje(@RequestBody Map<String, String> payload) {
        try {
            String nombre = payload.get("nombre");
            String email = payload.get("email");
            String asunto = payload.get("asunto");
            String mensaje = payload.get("mensaje");

            emailService.enviarCorreoSoporte(nombre, email, asunto, mensaje);

            return ResponseEntity.ok("Mensaje enviado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar la solicitud de soporte.");
        }
    }
}