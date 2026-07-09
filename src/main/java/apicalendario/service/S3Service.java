package apicalendario.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String subirPdf(MultipartFile archivo, String hojaId) throws IOException {
        String clave = "pdfs/" + hojaId + "/" + UUID.randomUUID() + ".pdf";

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(clave)
                .contentType("application/pdf")
                .contentLength(archivo.getSize())
                .build(),
            RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize())
        );

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + clave;
    }

    public void eliminarPdf(String url) {
        if (url == null || url.isBlank()) return;
        try {
            // Extraer la clave del objeto desde la URL
            String clave = url.substring(url.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(clave)
                .build());
        } catch (Exception e) {
            // Log pero no lanzar excepción para no bloquear otras operaciones
            System.err.println("Error eliminando PDF de S3: " + e.getMessage());
        }
    }
}
