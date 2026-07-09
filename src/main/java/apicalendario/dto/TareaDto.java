package apicalendario.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaDto {

    @NotBlank(message = "El nombre de la tarea no puede quedarn en blanco")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Debe tener una fecha de inicio")
    private LocalDateTime fechaInicio;

    @NotNull(message = "Debe tener una fecha de termino")
    private LocalDateTime fechaFin;

    private boolean esRecurrente;

    private String diasRecurrencia;

}
