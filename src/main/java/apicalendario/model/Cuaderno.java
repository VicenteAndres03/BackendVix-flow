package apicalendario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuaderno")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cuaderno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = true)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({ "password", "cuadernos", "tareas", "habitos", "rol" }) // 👈 evita exponer datos sensibles
    private User usuario;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(columnDefinition = "TEXT", nullable = true)
    private String fotoCuaderno;
}