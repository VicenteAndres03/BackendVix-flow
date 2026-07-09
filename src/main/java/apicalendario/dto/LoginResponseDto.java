package apicalendario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String nombre;
    private String apellido;
    private String email;
    private String rol; // ← agregar este campo
    private String estadoSuscripcion; // ← AGREGAR ESTE CAMPO
    // En LoginResponseDto.java agrega:
    private boolean pruebaConsumida;

}
