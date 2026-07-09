package apicalendario.service;

import apicalendario.model.Habito;
import apicalendario.model.User;
import apicalendario.repository.HabitoRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class HabitoService {

    private final HabitoRepository habitoRepo;
    private final UsuarioRepository usuarioRepo;

    public List<Habito> obtenerHabitosPorUsuario(String email) {
        User usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return habitoRepo.findByUsuario(usuario);
    }

    public Habito crearHabito(String email, String nombre) {
        User usuario = usuarioRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Habito nuevoHabito = Habito.builder()
                .nombre(nombre)
                .usuario(usuario)
                .build();
        return habitoRepo.save(nuevoHabito);
    }

    public Habito toggleFecha(Long id, String fecha) {
        Habito habito = habitoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hábito no encontrado"));

        LocalDate fechaParseada = LocalDate.parse(fecha);

        if (habito.getFechasCompletadas().contains(fechaParseada)) {
            habito.getFechasCompletadas().remove(fechaParseada);
        } else {
            habito.getFechasCompletadas().add(fechaParseada);
        }
        return habitoRepo.save(habito);
    }

    public void eliminarHabito(Long id) {
        habitoRepo.deleteById(id);
    }
}