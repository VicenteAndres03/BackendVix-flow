package apicalendario.controller;

import apicalendario.dto.HojaDto;
import apicalendario.model.Hoja;
import apicalendario.service.HojaService;
import apicalendario.service.S3Service;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hojas")
@AllArgsConstructor
public class HojaController {

    private final HojaService hojaService;
    private final S3Service s3Service;

    @GetMapping("/cuaderno/{cuadernoId}")
    public ResponseEntity<List<Hoja>> obtenerHojasDeCuaderno(@PathVariable Long cuadernoId) {
        return ResponseEntity.ok(hojaService.obtenerHojasDeCuaderno(cuadernoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hoja> obtenerHojaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(hojaService.obtenerHoja(id));
    }

    @PostMapping("/cuaderno/{cuadernoId}")
    public ResponseEntity<?> crearHoja(@PathVariable Long cuadernoId, @Valid @RequestBody HojaDto dto,
            Principal principal) {
        try {
            Hoja nuevaHoja = hojaService.crearHoja(principal.getName(), cuadernoId, dto);
            return new ResponseEntity<>(nuevaHoja, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hoja> actualizarHoja(@PathVariable Long id, @Valid @RequestBody HojaDto dto) {
        return ResponseEntity.ok(hojaService.actualizarHoja(id, dto));
    }

    // ── Subir PDF a S3 ──
    @PostMapping("/{id}/pdf")
    public ResponseEntity<?> subirPdf(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo) {
        try {
            if (archivo.isEmpty()) return ResponseEntity.badRequest().body("Archivo vacío");
            if (!archivo.getContentType().equals("application/pdf"))
                return ResponseEntity.badRequest().body("Solo se permiten archivos PDF");

            // Eliminar PDF anterior si existe
            Hoja hoja = hojaService.obtenerHoja(id);
            if (hoja.getUrlPdf() != null) s3Service.eliminarPdf(hoja.getUrlPdf());

            // Subir nuevo PDF a S3
            String url = s3Service.subirPdf(archivo, String.valueOf(id));

            // Guardar URL en la hoja
            Hoja actualizada = hojaService.actualizarUrlPdf(id, url);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error subiendo PDF: " + e.getMessage());
        }
    }

    // ── Eliminar PDF de S3 ──
    @DeleteMapping("/{id}/pdf")
    public ResponseEntity<?> eliminarPdf(@PathVariable Long id) {
        try {
            Hoja hoja = hojaService.obtenerHoja(id);
            if (hoja.getUrlPdf() != null) s3Service.eliminarPdf(hoja.getUrlPdf());
            Hoja actualizada = hojaService.actualizarUrlPdf(id, null);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error eliminando PDF: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarHoja(@PathVariable Long id) {
        // Eliminar PDF de S3 si existe antes de borrar la hoja
        try {
            Hoja hoja = hojaService.obtenerHoja(id);
            if (hoja.getUrlPdf() != null) s3Service.eliminarPdf(hoja.getUrlPdf());
        } catch (Exception ignored) {}
        return ResponseEntity.ok(hojaService.eliminarHoja(id));
    }
}
