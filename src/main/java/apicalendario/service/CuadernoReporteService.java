package apicalendario.service;

import apicalendario.model.Cuaderno;
import apicalendario.model.Hoja;
import apicalendario.repository.CuadernoRepository;
import apicalendario.repository.HojaRepository;
import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
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
                    agregarContenidoHtml(document, hoja.getContenido());
                }

                document.add(new Paragraph("───────────────────────────────────────────"));
            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF", e);
        }

        return out.toByteArray();
    }

    /**
     * Limpia HTML residual que no aporta nada al PDF (contenteditable,
     * estilos de posicionamiento de navegador que puedan colarse en algún
     * elemento que no sea imagen).
     */
    private String limpiarHtmlParaPdf(String html) {
        String limpio = html;
        limpio = limpio.replaceAll("position\\s*:\\s*[^;\"]+;?", "");
        limpio = limpio.replaceAll("display\\s*:\\s*inline-block;?", "");
        limpio = limpio.replaceAll("\\s*contenteditable=\"[^\"]*\"", "");
        return limpio;
    }

    /**
     * Convierte el HTML del editor en elementos PDF reales.
     *
     * IMPORTANTE: las imágenes se procesan APARTE del texto. HTMLWorker
     * mete las imágenes dentro del flujo de texto como elementos "inline"
     * sin escalarlas, lo que hace que se superpongan con el párrafo
     * siguiente. Por eso acá separamos manualmente cada <img>, la
     * decodificamos, la escalamos al ancho de la página, y la agregamos
     * como su propio bloque independiente — así nunca se mezcla con el
     * texto de al lado.
     */
    private static final java.util.regex.Pattern IMG_PATTERN = java.util.regex.Pattern.compile(
            "<img[^>]*src=\"data:image/[a-zA-Z]+;base64,([A-Za-z0-9+/=\\s]+)\"[^>]*>");

    private void agregarContenidoHtml(Document document, String contenidoHtml) throws DocumentException {
        java.util.regex.Matcher matcher = IMG_PATTERN.matcher(contenidoHtml);

        int ultimoFin = 0;
        while (matcher.find()) {
            String textoAntes = contenidoHtml.substring(ultimoFin, matcher.start());
            agregarTextoHtml(document, textoAntes);

            String base64Data = matcher.group(1).replaceAll("\\s", "");
            agregarImagenBase64(document, base64Data);

            ultimoFin = matcher.end();
        }

        String resto = contenidoHtml.substring(ultimoFin);
        agregarTextoHtml(document, resto);
    }

    private void agregarTextoHtml(Document document, String html) throws DocumentException {
        if (html == null || html.replaceAll("<[^>]*>", "").trim().isEmpty())
            return;

        try {
            HTMLWorker htmlWorker = new HTMLWorker(document);
            List<Element> elementos = htmlWorker.parseToList(new StringReader(limpiarHtmlParaPdf(html)), null);
            for (Element elemento : elementos) {
                document.add(elemento);
            }
        } catch (Exception e) {
            // Si el HTML viene mal formado (ej: tags sin cerrar del contentEditable),
            // caemos de vuelta a texto plano SIN las etiquetas, en vez de romper el PDF
            // completo.
            String textoPlano = html.replaceAll("<[^>]*>", "").trim();
            if (!textoPlano.isEmpty()) {
                Font fontFallback = FontFactory.getFont(FontFactory.HELVETICA, 11);
                Paragraph fallback = new Paragraph(textoPlano, fontFallback);
                fallback.setSpacingAfter(15);
                document.add(fallback);
            }
        }
    }

    private void agregarImagenBase64(Document document, String base64Data) throws DocumentException {
        try {
            byte[] bytes = java.util.Base64.getDecoder().decode(base64Data);
            Image imagen = Image.getInstance(bytes);

            float anchoDisponible = document.getPageSize().getWidth()
                    - document.leftMargin() - document.rightMargin();
            if (imagen.getWidth() > anchoDisponible) {
                imagen.scaleToFit(anchoDisponible, Float.MAX_VALUE);
            }

            imagen.setSpacingBefore(10f);
            imagen.setSpacingAfter(10f);
            imagen.setAlignment(Element.ALIGN_CENTER);
            document.add(imagen);
        } catch (Exception e) {
            // Si la imagen viene corrupta o no se puede decodificar, se omite
            // en vez de romper el PDF completo.
        }
    }
}