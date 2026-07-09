package apicalendario.service;

import java.util.List;

import org.springframework.stereotype.Service;

import apicalendario.dto.TareaProyectoDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.Estado;
import apicalendario.model.Proyecto;
import apicalendario.model.TareaProyecto;
import apicalendario.model.User;
import apicalendario.repository.ProyectoRepository;
import apicalendario.repository.TareaProyectoRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TareaProyectoService {

    private final TareaProyectoRepository repositorio;
    private final ProyectoRepository repositorioP;
    private final UsuarioRepository repositorioU;

    public String crearTarea(Long proyectoId, TareaProyectoDto dto) {
        Proyecto proyecto = repositorioP.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));

        User asignadoA = null;
        if (dto.getEmailAsignadoA() != null) {
            asignadoA = repositorioU.findByEmail(dto.getEmailAsignadoA())
                    .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario asignado no encontrado"));
        }

        TareaProyecto tarea = TareaProyecto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .fechaInicio(dto.getFechaInicio())
                .fechaLimite(dto.getFechaLimite())
                .estado(Estado.POR_HACER)
                .asignadoA(asignadoA)
                .proyecto(proyecto)
                .build();

        repositorio.save(tarea);
        return "Tarea creada correctamente";
    }

    public List<TareaProyecto> obtenerTareasPorProyecto(Long proyectoId) {
        Proyecto proyecto = repositorioP.findById(proyectoId)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
        return repositorio.findByProyecto(proyecto);
    }

    public String cambiarEstado(Long id, Estado nuevoEstado) {
        TareaProyecto tarea = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        tarea.setEstado(nuevoEstado);
        repositorio.save(tarea);
        return "Estado actualizado correctamente";
    }

    public String eliminarTarea(Long id) {
        TareaProyecto tarea = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        repositorio.delete(tarea);
        return "Tarea eliminada correctamente";
    }
}