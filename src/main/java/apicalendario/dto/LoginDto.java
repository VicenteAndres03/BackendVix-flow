package apicalendario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @Email
    @NotBlank(message = "El email no debe estar vacio")
    private String email;

    @Size(min = 8, message = "el password debe ser de al menos 8 caracteres")
    @NotBlank(message = "la contraseña no debe estar vacio")
    private String password;
}
