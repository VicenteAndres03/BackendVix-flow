package apicalendario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoDto {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    private String descripcion;
}