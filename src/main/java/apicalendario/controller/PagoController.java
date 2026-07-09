package apicalendario.controller;

import apicalendario.model.EstadoSuscripcion;
import apicalendario.model.User;
import apicalendario.repository.UsuarioRepository;
import apicalendario.security.JwtService;
import apicalendario.service.PagoService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preapproval.PreApprovalAutoRecurringCreateRequest;
import com.mercadopago.client.preapproval.PreapprovalClient;
import com.mercadopago.client.preapproval.PreapprovalCreateRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preapproval.Preapproval;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final PagoService pagoService;
    private final UsuarioRepository usuarioRepo;
    private final JwtService jwtService;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @PostMapping("/crear-suscripcion")
    public ResponseEntity<Map<String, String>> crearSuscripcion(@RequestParam String email) {
        try {
            // 🔥 NUEVO: Buscamos al usuario para obtener su ID
            User usuario = usuarioRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            PreapprovalClient client = new PreapprovalClient();

            PreApprovalAutoRecurringCreateRequest autoRecurring = PreApprovalAutoRecurringCreateRequest.builder()
                    .frequency(1)
                    .frequencyType("months")
                    .transactionAmount(new BigDecimal("3000"))
                    .currencyId("CLP")
                    .build();

            PreapprovalCreateRequest request = PreapprovalCreateRequest.builder()
                    .payerEmail(email)
                    .backUrl("https://vix-flow.com/perfil?pago=exitoso")
                    .reason("Suscripción Premium Vix-Flow")
                    .autoRecurring(autoRecurring)
                    .status("pending")
                    // 🔥 CAMBIADO: Usamos el ID del usuario en vez del email
                    .externalReference(String.valueOf(usuario.getId()))
                    .build();

            Preapproval preapproval = client.create(request);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("url", preapproval.getInitPoint());

            return ResponseEntity.ok(responseData);

        } catch (MPApiException apiException) {
            System.out.println("❌ Error MP: " + apiException.getApiResponse().getContent());
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cancelar-suscripcion")
    public ResponseEntity<String> cancelarSuscripcion(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = jwtService.extraerEmail(token);

            User usuario = usuarioRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String suscripcionId = usuario.getMercadoPagoSuscripcionId();

            if (suscripcionId == null) {
                return ResponseEntity.badRequest()
                        .body("No tienes una suscripción activa para cancelar.");
            }

            pagoService.cancelarSuscripcion(suscripcionId);

            usuario.setEstadoSuscripcion(EstadoSuscripcion.INACTIVO);
            usuario.setFechaFinPremium(null);
            usuario.setMercadoPagoSuscripcionId(null);
            usuarioRepo.save(usuario);

            return ResponseEntity.ok("Tu suscripción Premium ha sido cancelada exitosamente.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al cancelar la suscripción.");
        }
    }

    /**
     * CORRECCIÓN PRINCIPAL:
     * MercadoPago puede enviar el webhook de 3 formas distintas:
     * 1. Con `type` e `id` como query params (formato antiguo)
     * 2. Con todo en el body como JSON (formato nuevo)
     * 3. Mixto: type en query param, id en el body
     *
     * Antes el código fallaba silenciosamente cuando `dataId` era null,
     * respondía 200 OK y MP lo daba por procesado sin haber hecho nada.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> recibirWebhook(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String id) {
        try {
            // ── PASO 1: Asegurar que el token de MP esté siempre configurado ──
            // Necesario porque este endpoint se llama desde MP directamente,
            // no necesariamente después del @PostConstruct del Controller.
            MercadoPagoConfig.setAccessToken(accessToken);

            System.out.println("🔔 Webhook recibido - type: " + type + " | id: " + id
                    + " | body: " + payload);

            // ── PASO 2: Extraer dataId de TODAS las fuentes posibles ──
            String dataId = id; // primero intentamos el query param

            if (dataId == null && payload != null) {
                // Formato nuevo: { "data": { "id": "..." } }
                Object dataObj = payload.get("data");
                if (dataObj instanceof Map) {
                    Object idObj = ((Map<?, ?>) dataObj).get("id");
                    if (idObj != null) {
                        dataId = idObj.toString();
                    }
                }
                // Algunos eventos lo mandan directo en la raíz del body
                if (dataId == null && payload.get("id") != null) {
                    dataId = payload.get("id").toString();
                }
            }

            // ── PASO 3: Extraer el tipo del body si no vino en query param ──
            String tipo = type;
            if (tipo == null && payload != null) {
                tipo = (String) payload.get("type");
                // También puede venir como "action" en algunos eventos de MP
                if (tipo == null) {
                    tipo = (String) payload.get("action");
                }
            }

            System.out.println("🔔 Webhook normalizado - tipo: " + tipo + " | dataId: " + dataId);

            // ── PASO 4: Procesar solo si tenemos los datos mínimos ──
            if (dataId == null) {
                // CRÍTICO: Loguear para que puedas ver en logs que llegó sin ID
                System.out.println("⚠️ Webhook recibido sin dataId. Payload completo: " + payload
                        + " | type: " + type + " | id: " + id);
                // Aún así respondemos 200 para que MP no reintente infinitamente
                return ResponseEntity.ok("Recibido sin datos procesables");
            }

            if ("subscription_preapproval".equals(tipo)) {
                pagoService.procesarEventoSuscripcion(dataId);
            } else {
                System.out.println("ℹ️ Tipo de evento no procesado: " + tipo);
            }

            return ResponseEntity.ok("Recibido");

        } catch (Exception e) {
            System.out.println("❌ Error webhook: " + e.getMessage());
            e.printStackTrace();
            // Siempre 200 para evitar reintentos infinitos de MercadoPago
            return ResponseEntity.ok("Recibido");
        }
    }
}