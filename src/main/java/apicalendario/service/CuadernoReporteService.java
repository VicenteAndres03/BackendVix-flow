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
     * El editor web envuelve las imágenes en un <span class="img-wrapper"
     * style="position:relative;display:inline-block;..."> para poder
     * posicionar controles de UI encima de ellas (ej: un botón de borrar).
     * Esos estilos son conceptos de CSS de navegador que HTMLWorker no
     * entiende, y hacen que la imagen se solape con el texto siguiente
     * en el PDF en vez de quedar en su propio bloque.
     *
     * Aquí "desenvolvemos" la imagen de ese span y le agregamos un salto
     * de línea después, para forzarla a su propio espacio en el documento.
     * También quitamos cualquier "position:" o "display: inline-block"
     * suelto que quede en otros elementos, por la misma razón.
     */
    private String limpiarHtmlParaPdf(String html) {
        String limpio = html;

        // Desenvuelve <span class="img-wrapper" ...>...(img)...</span> dejando solo el
        // <img>,
        // y agrega un <br/> después para que quede en su propia línea.
        limpio = limpio.replaceAll(
                "(?s)<span[^>]*class=\"img-wrapper\"[^>]*>(.*?)</span>",
                "$1<br/>");

        // Por si queda algún position/display inline-block suelto en otros elementos
        limpio = limpio.replaceAll("position\\s*:\\s*[^;\"]+;?", "");
        limpio = limpio.replaceAll("display\\s*:\\s*inline-block;?", "");

        // contenteditable="false" no aporta nada en el PDF, solo ruido
        limpio = limpio.replaceAll("\\s*contenteditable=\"[^\"]*\"", "");

        return limpio;
    }

    /**
     * Convierte el HTML del editor (negrita, cursiva, subrayado, tamaños, etc.)
     * en elementos PDF reales usando HTMLWorker, en vez de imprimir las
     * etiquetas HTML como texto plano.
     */
    private void agregarContenidoHtml(Document document, String contenidoHtml) {
        try {
            String htmlLimpio = limpiarHtmlParaPdf(contenidoHtml);
            // HTMLWorker interpreta <b>, <i>, <u>, <p>, <br>, <span style="...">, etc.
            HTMLWorker htmlWorker = new HTMLWorker(document);
            List<Element> elementos = htmlWorker.parseToList(new StringReader(htmlLimpio), null);
            for (Element elemento : elementos) {
                document.add(elemento);
            }
        } catch (Exception e) {
            // Si el HTML viene mal formado (ej: tags sin cerrar del contentEditable),
            // caemos de vuelta a texto plano SIN las etiquetas, en vez de romper el PDF
            // completo.
            String textoPlano = contenidoHtml.replaceAll("<[^>]*>", "");
            Font fontFallback = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Paragraph fallback = new Paragraph(textoPlano, fontFallback);
            fallback.setSpacingAfter(15);
            try {
                document.add(fallback);
            } catch (DocumentException ignored) {
                // Última línea de defensa: si ni el fallback se puede agregar, se omite esta
                // hoja.
            }
        }
    }
}