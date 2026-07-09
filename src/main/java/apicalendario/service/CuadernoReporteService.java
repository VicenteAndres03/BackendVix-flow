package apicalendario.service;

import apicalendario.model.Cuaderno;
import apicalendario.model.Hoja;
import apicalendario.repository.CuadernoRepository;
import apicalendario.repository.HojaRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class CuadernoReporteService {

    private final CuadernoRepository cuadernoRepo;
    private final HojaRepository hojaRepo;

    public byte[] generarReporteCuadernoPdf(Long cuadernoId) {
        Cuaderno cuaderno = cuadernoRepo.findById(cuadernoId)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));

        List<Hoja> hojas = hojaRepo.findByCuaderno(cuaderno);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Paragraph titulo = new Paragraph("📓 " + cuaderno.getNombre(), fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(10);
            document.add(titulo);

            // Descripción
            if (cuaderno.getDescripcion() != null) {
                Font fontDesc = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12);
                Paragraph desc = new Paragraph(cuaderno.getDescripcion(), fontDesc);
                desc.setAlignment(Element.ALIGN_CENTER);
                desc.setSpacingAfter(5);
                document.add(desc);
            }

            // Fecha
            Font fontFecha = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph fecha = new Paragraph("Exportado el: " + LocalDate.now(), fontFecha);
            fecha.setAlignment(Element.ALIGN_CENTER);
            fecha.setSpacingAfter(20);
            document.add(fecha);

            // Separador
            document.add(new Paragraph("─────────────────────────────────────────────"));

            // Hojas
            for (Hoja hoja : hojas) {
                document.add(new Paragraph(" "));

                Font fontHojaTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                Paragraph tituloHoja = new Paragraph(hoja.getTitulo(), fontHojaTitulo);
                tituloHoja.setSpacingAfter(8);
                document.add(tituloHoja);

                if (hoja.getContenido() != null && !hoja.getContenido().isEmpty()) {
                    Font fontContenido = FontFactory.getFont(FontFactory.HELVETICA, 11);
                    Paragraph contenido = new Paragraph(hoja.getContenido(), fontContenido);
                    contenido.setSpacingAfter(15);
                    document.add(contenido);
                }

                document.add(new Paragraph("───────────────────────────────────────────"));
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF", e);
        }

        return out.toByteArray();
    }
}