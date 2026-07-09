package apicalendario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuadernoDto {

    @NotBlank(message = "El nombre del cuaderno no puede estar vacío")
    private String nombre;

    private String descripcion;

    private String fotoCuaderno;
}