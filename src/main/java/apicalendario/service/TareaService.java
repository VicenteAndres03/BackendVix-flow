package apicalendario.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import apicalendario.dto.TareaDto;
import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.Estado;
import apicalendario.model.Tarea;
import apicalendario.model.User;
import apicalendario.repository.TareaRepository;
import apicalendario.repository.UsuarioRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TareaService {

    private final TareaRepository repositorio;
    private final UsuarioRepository repositorioU;
    private final MetricasService metricasService;

    public String RegistrarTarea(String email, TareaDto tarea) {
        User usuario = repositorioU.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

        Tarea ta = Tarea.builder()
                .nombre(tarea.getNombre())
                .descripcion(tarea.getDescripcion())
                .fechaInicio(tarea.getFechaInicio())
                .fechaFin(tarea.getFechaFin())
                .estado(Estado.POR_HACER)
                .esRecurrente(tarea.isEsRecurrente())
                .diasRecurrencia(tarea.getDiasRecurrencia())
                .historialEstados("") // Inicializamos el historial vacío
                .usuario(usuario)
                .build();

        repositorio.save(ta);

        return "Tarea registrada de manera correcta";
    }

    // Método para obtener todas las tareas y pintar el calendario
    public List<Tarea> obtenerTodasLasTareas(String email) {
        User usuario = repositorioU.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return repositorio.findByUsuario(usuario);
    }

    // Método para obtener las tareas de un día específico con sus estados
    // independientes
    public List<Tarea> TareadelDia(String email, LocalDate fecha) {
        User usuario = repositorioU.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Este usuario no se encontro"));

        List<Tarea> todas = repositorio.findByUsuario(usuario);

        int dayOfWeekNum = fecha.getDayOfWeek().getValue();
        String[] mapaDias = { "", "L", "M", "X", "J", "V", "S", "D" };
        String letraDia = mapaDias[dayOfWeekNum];

        // 1. Filtramos qué tareas tocan este día
        List<Tarea> filtradas = todas.stream().filter(t -> {
            if (t.getFechaInicio() == null)
                return false;
            LocalDate fechaInicioTarea = t.getFechaInicio().toLocalDate();

            if (t.isEsRecurrente() && t.getDiasRecurrencia() != null) {
                return !fechaInicioTarea.isAfter(fecha) && t.getDiasRecurrencia().contains(letraDia);
            } else {
                return fechaInicioTarea.equals(fecha);
            }
        }).collect(Collectors.toList());

        // 2. Aplicamos los estados independientes por fecha usando el historial
        return filtradas.stream().map(t -> {
            if (t.isEsRecurrente() && t.getHistorialEstados() != null && !t.getHistorialEstados().isEmpty()) {
                String prefijoFecha = fecha.toString() + ":";
                String[] registros = t.getHistorialEstados().split(",");
                for (String registro : registros) {
                    if (registro.startsWith(prefijoFecha)) {
                        Estado estadoParaEseDia = Estado.valueOf(registro.split(":")[1]);
                        // Retornamos una copia exacta en memoria pero con el estado de ese día
                        return Tarea.builder()
                                .id(t.getId())
                                .nombre(t.getNombre())
                                .descripcion(t.getDescripcion())
                                .fechaInicio(t.getFechaInicio())
                                .fechaFin(t.getFechaFin())
                                .estado(estadoParaEseDia)
                                .esRecurrente(t.isEsRecurrente())
                                .diasRecurrencia(t.getDiasRecurrencia())
                                .historialEstados(t.getHistorialEstados())
                                .usuario(t.getUsuario())
                                .build();
                    }
                }
            }
            return t;
        }).collect(Collectors.toList());
    }

    // Método para cambiar el estado guardando en el historial si es recurrente
    public String cambiarEstado(Long id, Estado nuevoEstado, LocalDate fecha) {
        Tarea tarea = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        if (tarea.isEsRecurrente() && fecha != null) {
            // Si es recurrente, guardamos este movimiento en el historial
            String historial = tarea.getHistorialEstados() == null ? "" : tarea.getHistorialEstados();
            List<String> registros = new ArrayList<>(Arrays.asList(historial.split(",")));

            // Remover el registro viejo de ese día si existía previamente
            registros.removeIf(r -> r.startsWith(fecha.toString() + ":") || r.isEmpty());

            // Agregar el nuevo registro ("YYYY-MM-DD:ESTADO")
            registros.add(fecha.toString() + ":" + nuevoEstado.name());
            tarea.setHistorialEstados(String.join(",", registros));
        } else {
            // Si no es recurrente, simplemente cambiamos el estado original
            tarea.setEstado(nuevoEstado);
        }

        // 1. PRIMERO guardamos la tarea
        repositorio.save(tarea);

        // 2. LUEGO actualizamos la racha
        if (fecha != null) {
            metricasService.actualizarRacha(tarea.getUsuario().getEmail(), fecha);
        }

        return "Estado actualizado correctamente";
    }

    public String eliminarTarea(Long id) {
        Tarea tarea = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        repositorio.delete(tarea);
        return "Tarea eliminada de manera correcta";
    }

    public String actualizarTarea(Long id, TareaDto tarea) {
        Tarea ta = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        ta.setNombre(tarea.getNombre());
        ta.setDescripcion(tarea.getDescripcion());
        ta.setFechaInicio(tarea.getFechaInicio());
        ta.setFechaFin(tarea.getFechaFin());
        ta.setEsRecurrente(tarea.isEsRecurrente());
        ta.setDiasRecurrencia(tarea.getDiasRecurrencia());
        repositorio.save(ta);
        return "Tarea actualizada correctamente";
    }
}