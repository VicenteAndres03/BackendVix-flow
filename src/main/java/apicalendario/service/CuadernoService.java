package apicalendario.service;

import apicalendario.dto.CuadernoDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.Cuaderno;
import apicalendario.model.User;
import apicalendario.repository.CuadernoRepository;
import apicalendario.repository.HojaRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CuadernoService {

    private final CuadernoRepository cuadernoRepo;
    private final HojaRepository hojaRepo;
    private final UsuarioRepository usuarioRepo;

    public List<Cuaderno> obtenerCuadernosUsuario(String email) {
        User usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return cuadernoRepo.findByUsuario(usuario);
    }

    public Cuaderno crearCuaderno(String email, CuadernoDto dto) {
        User usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        // 🔒 LÓGICA FREEMIUM: Validar límites para usuarios gratuitos
        boolean esPremium = "ACTIVO".equals(usuario.getEstadoSuscripcion()) || "ADMIN".equals(usuario.getRol().name());

        if (!esPremium) {
            long totalCuadernos = cuadernoRepo.countByUsuario(usuario);
            if (totalCuadernos >= 2) {
                throw new RuntimeException(
                        "Has alcanzado el límite gratuito de 2 cuadernos. ¡Hazte Premium para tener cuadernos ilimitados!");
            }
        }

        Cuaderno nuevoCuaderno = Cuaderno.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fotoCuaderno(dto.getFotoCuaderno())
                .usuario(usuario)
                .build();

        return cuadernoRepo.save(nuevoCuaderno);
    }

    public Cuaderno actualizarCuaderno(Long id, CuadernoDto dto) {
        Cuaderno cuaderno = cuadernoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));

        cuaderno.setNombre(dto.getNombre());
        cuaderno.setDescripcion(dto.getDescripcion());

        return cuadernoRepo.save(cuaderno);
    }

    @Transactional
    public String eliminarCuaderno(Long id) {
        Cuaderno cuaderno = cuadernoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));

        // Primero borramos todas las hojas asociadas a este cuaderno
        hojaRepo.deleteByCuaderno(cuaderno);
        // Luego borramos el cuaderno
        cuadernoRepo.delete(cuaderno);

        return "Cuaderno eliminado correctamente";
    }

    public Cuaderno actualizarFotoCuaderno(Long id, String foto) {
        Cuaderno cuaderno = cuadernoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuaderno no encontrado"));
        cuaderno.setFotoCuaderno(foto);
        return cuadernoRepo.save(cuaderno);
    }
}