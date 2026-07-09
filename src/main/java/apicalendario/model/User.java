package apicalendario.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSuscripcion estadoSuscripcion;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Builder.Default
    @Column(nullable = false)
    private int rachaActual = 0;

    @Builder.Default
    @Column(nullable = false)
    private int mejorRacha = 0;

    @Builder.Default
    @Column(nullable = true)
    private LocalDate ultimoDiaProductivo = null;

    @Builder.Default
    @Column(nullable = false)
    private int diasGratuitos = 0; // contador para los 100 días → mes gratis

    private boolean pruebaConsumida = false;

    // Añade esta variable a tu modelo User
    private LocalDate fechaFinPremium;

    @Column(nullable = true)
    private String mercadoPagoSuscripcionId;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String fotoPerfil;

}
