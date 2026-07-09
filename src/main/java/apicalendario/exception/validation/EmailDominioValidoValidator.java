package apicalendario.exception.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class EmailDominioValidoValidator implements ConstraintValidator<EmailDominioValido, String> {

    private static final List<String> DOMINIOS_BLOQUEADOS = List.of(
            // Dominios de prueba
            "test.com", "fake.com", "example.com", "sample.com",
            // Correos temporales
            "mailinator.com", "tempmail.com", "guerrillamail.com",
            "throwam.com", "yopmail.com", "trashmail.com",
            "sharklasers.com", "guerrillamailblock.com", "grr.la",
            "spam4.me", "dispostable.com", "maildrop.cc",
            "tempr.email", "discard.email", "spamgourmet.com",
            "10minutemail.com", "minutemail.com", "temp-mail.org",
            "getnada.com", "mailnull.com", "spamspot.com",
            // Dominios .cl sospechosos
            "test.cl", "fake.cl", "temporal.cl", "prueba.cl");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null)
            return true;
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        return !DOMINIOS_BLOQUEADOS.contains(dominio);
    }
}