package apicalendario.service;

import apicalendario.exception.UsuarioNoEncontradoException;
import apicalendario.model.*;
import apicalendario.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MetricasService {

        private final TareaRepository tareaRepo;
        private final TareaProyectoRepository tareaProyectoRepo;
        private final ProyectoMiembroRepository proyectoMiembroRepo;
        private final UsuarioRepository usuarioRepo;

        // ─────────────────────────────────────────
        // RACHA - Se llama cada vez que el usuario
        // cambia el estado de una tarea
        // ─────────────────────────────────────────
        public void actualizarRacha(String email, LocalDate fecha) {
                User usuario = usuarioRepo.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

                LocalDateTime inicioDia = fecha.atStartOfDay();
                LocalDateTime finDia = fecha.atTime(23, 59, 59);

                // Obtenemos TODAS las tareas del usuario
                List<Tarea> todasLasTareas = tareaRepo.findByUsuario(usuario);

                // Filtramos las que aplican para ese día (igual que en
                // TareaService.TareadelDia)
                int dayOfWeekNum = fecha.getDayOfWeek().getValue();
                String[] mapaDias = { "", "L", "M", "X", "J", "V", "S", "D" };
                String letraDia = mapaDias[dayOfWeekNum];

                List<Tarea> tareasDelDia = todasLasTareas.stream().filter(t -> {
                        if (t.getFechaInicio() == null)
                                return false;
                        LocalDate fechaInicioTarea = t.getFechaInicio().toLocalDate();

                        if (t.isEsRecurrente() && t.getDiasRecurrencia() != null) {
                                return !fechaInicioTarea.isAfter(fecha) &&
                                                t.getDiasRecurrencia().contains(letraDia);
                        } else {
                                return fechaInicioTarea.equals(fecha);
                        }
                }).collect(java.util.stream.Collectors.toList());

                if (tareasDelDia.isEmpty())
                        return;

                // Anti-trampa: nombres únicos en ese día
                long tareasUnicas = tareasDelDia.stream()
                                .map(t -> t.getNombre().toLowerCase().trim())
                                .distinct()
                                .count();

                if (tareasUnicas < 3)
                        return;

                // Para tareas recurrentes, obtenemos el estado real de ese día
                long completadas = tareasDelDia.stream().filter(t -> {
                        if (t.isEsRecurrente() && t.getHistorialEstados() != null
                                        && !t.getHistorialEstados().isEmpty()) {
                                String prefijoFecha = fecha.toString() + ":";
                                for (String registro : t.getHistorialEstados().split(",")) {
                                        if (registro.startsWith(prefijoFecha)) {
                                                return Estado.TERMINADO.name().equals(registro.split(":")[1]);
                                        }
                                }
                                return t.getEstado() == Estado.TERMINADO;
                        }
                        return t.getEstado() == Estado.TERMINADO;
                }).count();

                double porcentaje = (completadas * 100.0) / tareasDelDia.size();

                if (porcentaje >= 80) {
                        LocalDate ultimoDia = usuario.getUltimoDiaProductivo();

                        if (ultimoDia == null) {
                                usuario.setRachaActual(1);
                        } else if (ultimoDia.equals(fecha.minusDays(1))) {
                                usuario.setRachaActual(usuario.getRachaActual() + 1);
                        } else if (!ultimoDia.equals(fecha)) {
                                usuario.setRachaActual(1);
                        }

                        if (usuario.getRachaActual() > usuario.getMejorRacha()) {
                                usuario.setMejorRacha(usuario.getRachaActual());
                        }

                        usuario.setDiasGratuitos(usuario.getDiasGratuitos() + 1);
                        usuario.setUltimoDiaProductivo(fecha);
                        usuarioRepo.save(usuario);
                }
        }

        // ─────────────────────────────────────────
        // MÉTRICAS PERSONALES
        // ─────────────────────────────────────────
        public Map<String, Object> obtenerMetricasPersonales(String email) {
                User usuario = usuarioRepo.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

                List<Tarea> todasLasTareas = tareaRepo.findByUsuario(usuario);

                // Totales generales
                long totalCreadas = todasLasTareas.size();
                long totalCompletadas = todasLasTareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO)
                                .count();
                double porcentajeGeneral = totalCreadas == 0 ? 0
                                : Math.round((totalCompletadas * 100.0) / totalCreadas * 10.0) / 10.0;

                // Esta semana vs semana pasada
                LocalDate hoy = LocalDate.now();
                LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                LocalDate inicioSemanaPasada = inicioSemana.minusDays(7);

                long completadasEstaSemana = todasLasTareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO && t.getFechaInicio() != null)
                                .filter(t -> {
                                        LocalDate fecha = t.getFechaInicio().toLocalDate();
                                        return !fecha.isBefore(inicioSemana) && !fecha.isAfter(hoy);
                                }).count();

                long completadasSemanaPasada = todasLasTareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO && t.getFechaInicio() != null)
                                .filter(t -> {
                                        LocalDate fecha = t.getFechaInicio().toLocalDate();
                                        return !fecha.isBefore(inicioSemanaPasada) && fecha.isBefore(inicioSemana);
                                }).count();

                // Hora más productiva
                Map<Integer, Long> tareasPorHora = todasLasTareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO && t.getFechaInicio() != null)
                                .collect(Collectors.groupingBy(
                                                t -> t.getFechaInicio().getHour(),
                                                Collectors.counting()));

                int horaMasProductiva = tareasPorHora.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse(-1);

                // Día más productivo
                Map<String, Long> tareasPorDia = todasLasTareas.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO && t.getFechaInicio() != null)
                                .collect(Collectors.groupingBy(
                                                t -> t.getFechaInicio().getDayOfWeek().toString(),
                                                Collectors.counting()));

                String diaMasProductivo = tareasPorDia.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("N/A");

                // Promedio diario (últimos 30 días)
                long diasConTareas = todasLasTareas.stream()
                                .filter(t -> t.getFechaInicio() != null)
                                .map(t -> t.getFechaInicio().toLocalDate())
                                .distinct()
                                .count();

                double promedioDiario = diasConTareas == 0 ? 0
                                : Math.round((totalCompletadas * 10.0) / diasConTareas) / 10.0;

                // ── Tareas de HOY por estado ──
                int dayOfWeekNum = hoy.getDayOfWeek().getValue();
                String[] mapaDias = { "", "L", "M", "X", "J", "V", "S", "D" };
                String letraHoy = mapaDias[dayOfWeekNum];

                List<Tarea> tareasDeHoy = todasLasTareas.stream().filter(t -> {
                        if (t.getFechaInicio() == null)
                                return false;
                        LocalDate fechaTarea = t.getFechaInicio().toLocalDate();
                        if (t.isEsRecurrente() && t.getDiasRecurrencia() != null) {
                                return !fechaTarea.isAfter(hoy) && t.getDiasRecurrencia().contains(letraHoy);
                        } else {
                                return fechaTarea.equals(hoy);
                        }
                }).collect(Collectors.toList());

                long hoyPorHacer = tareasDeHoy.stream()
                                .filter(t -> t.getEstado() == Estado.POR_HACER).count();
                long hoyEnProceso = tareasDeHoy.stream()
                                .filter(t -> t.getEstado() == Estado.EN_PROCESO).count();
                long hoyCompletadas = tareasDeHoy.stream()
                                .filter(t -> t.getEstado() == Estado.TERMINADO).count();

                // Armamos la respuesta
                Map<String, Object> metricas = new LinkedHashMap<>();
                metricas.put("totalCreadas", totalCreadas);
                metricas.put("totalCompletadas", totalCompletadas);
                metricas.put("porcentajeGeneral", porcentajeGeneral);
                metricas.put("completadasEstaSemana", completadasEstaSemana);
                metricas.put("completadasSemanaPasada", completadasSemanaPasada);
                metricas.put("horaMasProductiva", horaMasProductiva == -1 ? "Sin datos" : horaMasProductiva + ":00");
                metricas.put("diaMasProductivo", traducirDia(diaMasProductivo));
                metricas.put("promedioDiario", promedioDiario);
                metricas.put("rachaActual", usuario.getRachaActual());
                metricas.put("mejorRacha", usuario.getMejorRacha());
                metricas.put("diasGratuitos", usuario.getDiasGratuitos());
                metricas.put("hoyPorHacer", hoyPorHacer);
                metricas.put("hoyEnProceso", hoyEnProceso);
                metricas.put("hoyCompletadas", hoyCompletadas);

                return metricas;
        }

        // ─────────────────────────────────────────
        // MÉTRICAS DE PROYECTOS
        // ─────────────────────────────────────────
        public Map<String, Object> obtenerMetricasProyectos(String email) {
                User usuario = usuarioRepo.findByEmail(email)
                                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));

                List<ProyectoMiembro> misProyectos = proyectoMiembroRepo
                                .findByUsuarioAndEstado(usuario, EstadoInvitacion.ACEPTADO);

                List<Map<String, Object>> proyectosDetalle = new ArrayList<>();

                for (ProyectoMiembro pm : misProyectos) {
                        Proyecto proyecto = pm.getProyecto();
                        List<TareaProyecto> tareas = tareaProyectoRepo.findByProyecto(proyecto);

                        long totalTareas = tareas.size();
                        long completadas = tareas.stream()
                                        .filter(t -> t.getEstado() == Estado.TERMINADO)
                                        .count();
                        int progreso = totalTareas == 0 ? 0 : (int) ((completadas * 100) / totalTareas);

                        // Miembro más activo (el que tiene más tareas asignadas completadas)
                        Map<String, Long> tareasPorMiembro = tareas.stream()
                                        .filter(t -> t.getEstado() == Estado.TERMINADO && t.getAsignadoA() != null)
                                        .collect(Collectors.groupingBy(
                                                        t -> t.getAsignadoA().getNombre(),
                                                        Collectors.counting()));

                        String miembroMasActivo = tareasPorMiembro.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse("Sin datos");

                        // Tareas por estado
                        long porHacer = tareas.stream().filter(t -> t.getEstado() == Estado.POR_HACER).count();
                        long enProceso = tareas.stream().filter(t -> t.getEstado() == Estado.EN_PROCESO).count();

                        Map<String, Object> detalle = new LinkedHashMap<>();
                        detalle.put("id", proyecto.getId());
                        detalle.put("nombre", proyecto.getNombre());
                        detalle.put("totalTareas", totalTareas);
                        detalle.put("completadas", completadas);
                        detalle.put("porHacer", porHacer);
                        detalle.put("enProceso", enProceso);
                        detalle.put("progreso", progreso);
                        detalle.put("miembroMasActivo", miembroMasActivo);
                        detalle.put("totalMiembros", proyectoMiembroRepo.findByProyecto(proyecto).size());

                        proyectosDetalle.add(detalle);
                }

                Map<String, Object> respuesta = new LinkedHashMap<>();
                respuesta.put("totalProyectos", misProyectos.size());
                respuesta.put("proyectos", proyectosDetalle);

                return respuesta;
        }

        // ─────────────────────────────────────────
        // HELPER
        // ─────────────────────────────────────────
        private String traducirDia(String dia) {
                return switch (dia) {
                        case "MONDAY" -> "Lunes";
                        case "TUESDAY" -> "Martes";
                        case "WEDNESDAY" -> "Miércoles";
                        case "THURSDAY" -> "Jueves";
                        case "FRIDAY" -> "Viernes";
                        case "SATURDAY" -> "Sábado";
                        case "SUNDAY" -> "Domingo";
                        default -> dia;
                };
        }
}