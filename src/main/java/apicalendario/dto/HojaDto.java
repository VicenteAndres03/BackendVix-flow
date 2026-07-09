package apicalendario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HojaDto {

    @NotBlank(message = "El título de la hoja no puede estar vacío")
    private String titulo;

    private String contenido;

    // Anotaciones del canvas PDF (JSON array de dataURLs por página)
    private String anotacionesPdf;
}
