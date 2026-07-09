package apicalendario.service;

import apicalendario.model.Proyecto;
import apicalendario.model.TareaProyecto;
import apicalendario.repository.ProyectoRepository;
import apicalendario.repository.TareaProyectoRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ReporteService {

    private final ProyectoRepository proyectoRepo;
    private final TareaProyectoRepository tareaRepo;

    public byte[] generarReporteProyectoPdf(Long proyectoId) {
        Proyecto proyecto = proyectoRepo.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        List<TareaProyecto> tareas = tareaRepo.findByProyecto(proyecto);

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Título del Documento
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Paragraph titulo = new Paragraph("Reporte de Vix-Flow: " + proyecto.getNombre(), fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(15);
            document.add(titulo);

            // 2. Información del Proyecto
            Font fontTexto = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Descripción: " + proyecto.getDescripcion(), fontTexto));
            document.add(new Paragraph("Fecha de emisión: " + LocalDate.now().toString(), fontTexto));
            document.add(new Paragraph("Total de Tareas: " + tareas.size(), fontTexto));
            document.add(new Paragraph(" ")); // Salto de línea

            // 3. Tabla de Tareas (4 columnas)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3f, 2f, 2f, 2f });

            // Encabezados de la tabla
            String[] headers = { "Nombre de Tarea", "Estado", "Asignado A", "Fecha Límite" };
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Datos de la tabla
            for (TareaProyecto tarea : tareas) {
                table.addCell(new PdfPCell(new Phrase(tarea.getNombre())));
                table.addCell(new PdfPCell(new Phrase(tarea.getEstado().name())));

                String asignado = (tarea.getAsignadoA() != null) ? tarea.getAsignadoA().getNombre() : "Sin asignar";
                table.addCell(new PdfPCell(new Phrase(asignado)));

                String limite = (tarea.getFechaLimite() != null) ? tarea.getFechaLimite().toString() : "N/A";
                table.addCell(new PdfPCell(new Phrase(limite)));
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el archivo PDF", e);
        }

        return out.toByteArray();
    }
}