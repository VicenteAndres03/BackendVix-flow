package apicalendario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilDto {

    @NotBlank(message = "El campo nombre no puede quedar vacio")
    private String nombre;

    @NotBlank(message = "El campo apellido no puede quedar en blanco")
    private String apellido;

    @NotBlank(message = "El campo de password no puede quedar en blanco")
    @Size(min = 8)
    private String password;

}
