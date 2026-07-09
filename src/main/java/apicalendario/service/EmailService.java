package apicalendario.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreoBienvenida(String destinatario, String nombre) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        // El "From" idealmente debe coincidir con tu MAIL_USERNAME
        mensaje.setFrom("tu_correo@gmail.com");
        mensaje.setTo(destinatario);
        mensaje.setSubject("¡Bienvenido a Vix-Flow, " + nombre + "! Empieza a organizar tu vida 🚀");

        mensaje.setText("Hola " + nombre + ",\n\n"
                + "¡Qué alegría tenerte con nosotros! Te damos la bienvenida oficial a Vix-Flow, tu nuevo espacio centralizado para dominar tu tiempo y organizar tus proyectos con éxito.\n\n"
                + "Al crear tu cuenta, ya tienes acceso inmediato a nuestro Plan Gratuito (para siempre), con el cual puedes disfrutar de:\n"
                + "- 📅 Calendario Inteligente y Tablero Kanban.\n"
                + "- 🗂️ Creación de tareas y proyectos ilimitados.\n"
                + "- 👥 Sistema de amigos para organizar tu red de contactos.\n\n"
                + "💎 ¿Quieres llevar tu productividad al siguiente nivel?\n"
                + "Si buscas potenciar tu flujo de trabajo, nuestro Plan Premium está diseñado para darte el control absoluto:\n"
                + "- 🌱 Seguimiento de Hábitos para construir tu rutina ideal.\n"
                + "- 📊 Métricas de Productividad y Estadísticas detalladas.\n"
                + "- ⏱️ Temporizador Pomodoro integrado para sesiones de máximo enfoque.\n"
                + "- 🏆 Sistema de Gamificación con medallas y temas visuales exclusivos.\n"
                + "- 📄 Exportación automática de reportes profesionales a PDF.\n"
                + "- 🔗 Portal de Clientes mediante enlaces públicos y seguros.\n"
                + "(¡Recuerda que tienes 1 mes completamente gratis de Premium para probarlo todo!)\n\n"
                + "Para empezar hoy mismo, simplemente inicia sesión en la plataforma y crea tu primera tarea en el tablero.\n\n"
                + "Si en algún momento tienes dudas, sugerencias o necesitas ayuda, nuestro equipo estará encantado de escucharte.\n\n"
                + "¡Mucho éxito en todos tus proyectos!\n\n"
                + "Atentamente,\n"
                + "El equipo de Vix-Flow.");

        mailSender.send(mensaje);
    }

    public void enviarCorreoInvitacionProyecto(String destinatario, String nombreProyecto, String token) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("tu_correo@gmail.com");
        mensaje.setTo(destinatario);
        mensaje.setSubject("Invitación de equipo: Únete al proyecto \"" + nombreProyecto + "\" 🤝");

        // Enlace que redirigirá a tu frontend en React
        String urlAceptar = "https://vix-flow.com/aceptar-invitacion/" + token;

        mensaje.setText("Hola,\n\n"
                + "¡Excelentes noticias! Has sido invitado a formar parte del proyecto '" + nombreProyecto
                + "' en Vix-Flow.\n\n"
                + "El creador del proyecto te ha seleccionado para colaborar juntos. Al unirte, podrás:\n"
                + "- Ver y actualizar el estado de las tareas en tiempo real.\n"
                + "- Ser asignado a responsabilidades específicas.\n"
                + "- Contribuir al progreso global del equipo.\n\n"
                + "Para aceptar la invitación y entrar al tablero de trabajo, simplemente haz clic en el siguiente enlace:\n\n"
                + "▶ " + urlAceptar + "\n\n"
                + "(Nota: Si el enlace no funciona al hacer clic, cópialo y pégalo directamente en la barra de direcciones de tu navegador web).\n\n"
                + "Si no tienes relación con este proyecto o consideras que este correo es un error, puedes ignorar este mensaje de forma segura.\n\n"
                + "¡Es hora de lograr grandes resultados en equipo!\n\n"
                + "Atentamente,\n"
                + "El equipo de Vix-Flow.");

        mailSender.send(mensaje);
    }

    public void enviarCorreoRecuperacion(String destinatario, String nuevaPassword) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom("tu_correo@gmail.com");
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de acceso y seguridad de tu cuenta - Vix-Flow 🔒");

        mensaje.setText("Hola,\n\n"
                + "Hemos recibido una solicitud para recuperar el acceso a tu cuenta de Vix-Flow. Entendemos lo importante que es tu información, así que estamos aquí para ayudarte a entrar de nuevo.\n\n"
                + "Nuestro sistema de seguridad ha generado una clave temporal única para ti. Utiliza las siguientes credenciales para iniciar sesión:\n\n"
                + "▶ Contraseña temporal: " + nuevaPassword + "\n\n"
                + "⚠️ IMPORTANTE: PASOS A SEGUIR\n"
                + "1. Dirígete a la página de inicio de sesión de Vix-Flow.\n"
                + "2. Ingresa utilizando esta contraseña temporal.\n"
                + "3. Ve de inmediato a la sección 'Mi Perfil' / 'Ajustes de Seguridad'.\n"
                + "4. Cambia esta contraseña temporal por una nueva clave segura que solo tú conozcas.\n\n"
                + "Si tú no solicitaste recuperar tu contraseña, es posible que alguien haya intentado acceder a tu cuenta. En ese caso, te sugerimos iniciar sesión con tu clave habitual o con esta clave temporal y cambiarla de inmediato para mantener tu cuenta protegida.\n\n"
                + "Atentamente,\n"
                + "El equipo de Seguridad de Vix-Flow.");

        mailSender.send(mensaje);
    }

    public void enviarCorreoSoporte(String nombre, String emailUsuario, String asunto, String mensajeUsuario) {
        SimpleMailMessage mensaje = new SimpleMailMessage();

        // El correo desde donde se envía (tu cuenta de Spring Boot)
        mensaje.setFrom("tu_correo@gmail.com");

        // A dónde llegará el mensaje (Tu correo corporativo o personal para leer los
        // tickets)
        mensaje.setTo("soporte@vincitech.cl");

        mensaje.setSubject("NUEVO TICKET DE SOPORTE: " + asunto);
        mensaje.setText("Has recibido un nuevo mensaje de contacto desde Vix-Flow.\n\n"
                + "Detalles del usuario:\n"
                + "- Nombre: " + nombre + "\n"
                + "- Email: " + emailUsuario + "\n"
                + "- Asunto: " + asunto + "\n\n"
                + "Mensaje:\n"
                + mensajeUsuario + "\n\n"
                + "--------------------------------------------------------\n"
                + "Puedes responder a este ticket escribiendo directamente a: " + emailUsuario);

        mailSender.send(mensaje);
    }
}