package apicalendario.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habitos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    // 🔥 TRUCO MÁGICO: Guarda una lista de fechas directamente sin crear otra
    // entidad
    @ElementCollection
    @CollectionTable(name = "habito_fechas", joinColumns = @JoinColumn(name = "habito_id"))
    @Column(name = "fecha")
    @Builder.Default
    private Set<LocalDate> fechasCompletadas = new HashSet<>();
}