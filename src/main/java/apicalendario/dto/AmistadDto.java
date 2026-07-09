package apicalendario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmistadDto {

    @Email
    @NotBlank(message = "El email no puede estar vacío")
    private String emailReceptor;
}