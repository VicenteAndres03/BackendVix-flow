package apicalendario.service;

import apicalendario.dto.HojaDto;
import apicalendario.model.Cuaderno;
import apicalendario.model.Hoja;
import apicalendario.model.User;
import apicalendario.repository.CuadernoRepository;
import apicalendario.repository.HojaRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class HojaService {

    private final HojaRepository hojaRepo;
    private final CuadernoRepository cuadernoRepo;
    private final UsuarioRepository usuarioRepo;

    public List<Hoja> obtenerHojasDeCuaderno(Long cuadernoId) {
        Cuaderno cuaderno = cuadernoRepo.findById(cuadernoId)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));
        return hojaRepo.findByCuaderno(cuaderno);
    }

    public Hoja obtenerHoja(Long id) {
        return hojaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
    }

    public Hoja crearHoja(String email, Long cuadernoId, HojaDto dto) {
        User usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cuaderno cuaderno = cuadernoRepo.findById(cuadernoId)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));

        boolean esPremium = "ACTIVO".equals(usuario.getEstadoSuscripcion())
                || "ADMIN".equals(usuario.getRol().name());
        if (!esPremium) {
            long totalHojas = hojaRepo.countByCuaderno(cuaderno);
            if (totalHojas >= 5) {
                throw new RuntimeException(
                        "Has alcanzado el límite gratuito de 5 hojas por cuaderno. ¡Hazte Premium para tener hojas ilimitadas!");
            }
        }

        Hoja nuevaHoja = Hoja.builder()
                .titulo(dto.getTitulo())
                .contenido(dto.getContenido())
                .anotacionesPdf(null)
                .urlPdf(null)
                .cuaderno(cuaderno)
                .build();
        return hojaRepo.save(nuevaHoja);
    }

    public Hoja actualizarHoja(Long id, HojaDto dto) {
        Hoja hoja = hojaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
        hoja.setTitulo(dto.getTitulo());
        hoja.setContenido(dto.getContenido());
        if (dto.getAnotacionesPdf() != null) hoja.setAnotacionesPdf(dto.getAnotacionesPdf());
        hoja.setFechaActualizacion(LocalDateTime.now());
        return hojaRepo.save(hoja);
    }

    public Hoja actualizarUrlPdf(Long id, String url) {
        Hoja hoja = hojaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
        hoja.setUrlPdf(url);
        // Si se quita el PDF, limpiar también las anotaciones
        if (url == null) hoja.setAnotacionesPdf(null);
        hoja.setFechaActualizacion(LocalDateTime.now());
        return hojaRepo.save(hoja);
    }

    public String eliminarHoja(Long id) {
        Hoja hoja = hojaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hoja no encontrada"));
        hojaRepo.delete(hoja);
        return "Hoja eliminada correctamente";
    }
}
