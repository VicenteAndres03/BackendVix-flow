package apicalendario.dto;

import apicalendario.exception.validation.EmailDominioValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    @Email
    @NotBlank(message = "El email no puede quedar en blanco")
    @EmailDominioValido
    private String email;

    @NotBlank(message = "El campo nombre no puede quedar vacio")
    private String nombre;

    @NotBlank(message = "El campo apellido no puede quedar en blanco")
    private String apellido;

    @NotBlank(message = "El campo de password no puede quedar en blanco")
    @Size(min = 8)
    private String password;

}