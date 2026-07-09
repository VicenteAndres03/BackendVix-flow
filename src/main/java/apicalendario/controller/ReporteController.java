package apicalendario.controller;

import apicalendario.service.CuadernoReporteService;
import apicalendario.service.ReporteService;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@AllArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/proyecto/{id}")
    public ResponseEntity<byte[]> descargarReporteProyecto(@PathVariable Long id) {
        byte[] pdfBytes = reporteService.generarReporteProyectoPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // Le indicamos al navegador que es un archivo adjunto descargable
        headers.setContentDispositionFormData("attachment", "Reporte_Proyecto.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Autowired
    private CuadernoReporteService cuadernoReporteService;

    @GetMapping("/cuaderno/{id}")
    public ResponseEntity<byte[]> descargarReporteCuaderno(@PathVariable Long id) {
        byte[] pdfBytes = cuadernoReporteService.generarReporteCuadernoPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Cuaderno.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}