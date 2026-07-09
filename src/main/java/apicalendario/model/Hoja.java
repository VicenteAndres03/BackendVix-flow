package apicalendario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hoja")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hoja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String contenido;

    // Anotaciones del canvas PDF (JSON array de dataURLs por página)
    @Column(columnDefinition = "TEXT", nullable = true)
    private String anotacionesPdf;

    // URL del PDF almacenado en S3
    @Column(nullable = true)
    private String urlPdf;

    @ManyToOne
    @JoinColumn(name = "cuaderno_id", nullable = false)
    @JsonIgnoreProperties({ "usuario", "hojas" })
    private Cuaderno cuaderno;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
}
