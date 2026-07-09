package apicalendario.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "amistad")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private User solicitante;

    @ManyToOne
    @JoinColumn(name = "receptor_id", nullable = false)
    private User receptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoAmistad estado = EstadoAmistad.PENDIENTE;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
}