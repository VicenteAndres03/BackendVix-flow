package apicalendario.service;

import lombok.RequiredArgsConstructor;
import apicalendario.model.EstadoSuscripcion;
import apicalendario.repository.UsuarioRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.resources.preapproval.Preapproval;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final UsuarioRepository usuarioRepo;

    /**
     * CORRECCIÓN: Inyectamos el accessToken aquí también.
     * El @PostConstruct del Controller solo corre una vez al arrancar,
     * pero si el webhook llega antes o en otro hilo, el token puede no
     * estar configurado y PreapprovalClient falla silenciosamente.
     */
    @Value("${mercadopago.access-token}")
    private String accessToken;

    public void procesarEventoSuscripcion(String suscripcionId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreapprovalClient client = new PreapprovalClient();
            Preapproval preapproval = client.get(suscripcionId);

            String estado = preapproval.getStatus();
            // 🔥 CAMBIADO: externalReference ahora es el ID del usuario
            String usuarioId = preapproval.getExternalReference();

            System.out.println("🔔 Webhook - Estado: " + estado + " | UsuarioId: " + usuarioId
                    + " | SuscripcionId: " + suscripcionId);

            if (usuarioId == null || usuarioId.isBlank()) {
                System.out.println("⚠️ externalReference vacío para suscripcionId: " + suscripcionId);
                return;
            }

            // 🔥 CAMBIADO: Buscamos por ID en vez de email
            Long id = Long.parseLong(usuarioId);
            usuarioRepo.findById(id).ifPresent(usuario -> {

                if ("authorized".equals(estado) || "active".equals(estado)) {
                    usuario.setEstadoSuscripcion(EstadoSuscripcion.ACTIVO);
                    usuario.setMercadoPagoSuscripcionId(suscripcionId);
                    usuario.setFechaFinPremium(java.time.LocalDate.now().plusMonths(1));
                    usuarioRepo.save(usuario);
                    System.out.println("✅ Usuario activado ID: " + usuarioId);

                } else if ("cancelled".equals(estado) || "paused".equals(estado)) {
                    usuario.setEstadoSuscripcion(EstadoSuscripcion.INACTIVO);
                    usuario.setFechaFinPremium(null);
                    usuario.setMercadoPagoSuscripcionId(null);
                    usuarioRepo.save(usuario);
                    System.out.println("❌ Usuario desactivado ID: " + usuarioId);

                } else {
                    System.out.println("ℹ️ Estado no manejado: '" + estado + "' para usuario ID: " + usuarioId);
                }
            });

            if (usuarioRepo.findById(id).isEmpty()) {
                System.out.println("⚠️ No se encontró usuario con ID: " + usuarioId);
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ externalReference no es un ID válido: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Error procesando webhook para suscripcionId " + suscripcionId
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancelarSuscripcion(String suscripcionId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            com.mercadopago.client.preapproval.PreapprovalUpdateRequest updateRequest = com.mercadopago.client.preapproval.PreapprovalUpdateRequest
                    .builder()
                    .status("cancelled")
                    .build();

            PreapprovalClient client = new PreapprovalClient();
            client.update(suscripcionId, updateRequest);
            System.out.println("✅ Suscripción cancelada en MP: " + suscripcionId);

        } catch (Exception e) {
            System.out.println("❌ Error cancelando en MP: " + e.getMessage());
            throw new RuntimeException("Error al cancelar en Mercado Pago");
        }
    }
}
