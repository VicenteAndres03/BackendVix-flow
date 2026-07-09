# 📅 API Calendario

Sistema de calendarización personal que permite organizar el día a día mediante horarios, tareas recurrentes y eventos únicos. Diseñado para múltiples usuarios con soporte de pagos integrado.

---

## 🛠️ Tecnologías utilizadas

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React_Vite-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

---

## 📦 Dependencias principales

| Dependencia       | Versión | Propósito                       |
| ----------------- | ------- | ------------------------------- |
| Spring Web        | 3.2.x   | Endpoints REST                  |
| Spring Data JPA   | 3.2.x   | Conexión a base de datos        |
| Spring Security   | 3.2.x   | Seguridad y autenticación       |
| PostgreSQL Driver | 42.x    | Comunicación con PostgreSQL     |
| Lombok            | 1.18.x  | Reducción de código repetitivo  |
| Java Mail Sender  | 3.2.x   | Envío de correos con Gmail      |
| Validation        | 3.2.x   | Validación de datos entrantes   |
| JJWT              | 0.11.x  | Manejo de tokens JWT            |
| Spring Dotenv     | 4.0.0   | Lectura de variables de entorno |

---

## ⚙️ Variables de entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/apicalendario
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña
DB_DDL_AUTO=update

# JWT
JWT_SECRET=tu_clave_secreta
JWT_EXPIRATION=86400000

# Correo Gmail
MAIL_USERNAME=tu_correo@gmail.com
MAIL_PASSWORD=tu_app_password
```

> ⚠️ **Nunca subas el archivo `.env` a GitHub.** Ya está incluido en el `.gitignore`.

---

## 🗂️ Estructura del proyecto

```
src/main/java/com/apicalendario/
├── config/         # Configuración general y CORS
├── controller/     # Endpoints REST
├── dto/            # Objetos de transferencia de datos
├── exception/      # Manejo global de errores
├── model/          # Entidades de la base de datos
├── repository/     # Acceso a datos
├── security/       # JWT y filtros de seguridad
└── service/        # Lógica de negocio
```

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos previos

- Java 21
- Maven
- PostgreSQL
- Node.js (para el frontend)

### Pasos

**1. Clona el repositorio**

```bash
git clone https://github.com/tu-usuario/apicalendario.git
cd apicalendario
```

**2. Crea la base de datos en PostgreSQL**

```sql
CREATE DATABASE apicalendario;
CREATE USER tu_usuario WITH PASSWORD 'tu_contraseña';
GRANT ALL PRIVILEGES ON DATABASE apicalendario TO tu_usuario;
```

**3. Configura el archivo `.env`**

Copia el ejemplo de variables de entorno de la sección anterior y completa con tus datos.

**4. Ejecuta el backend**

```bash
./mvnw spring-boot:run
```

El servidor estará disponible en: `http://localhost:8080`

**5. Ejecuta el frontend** _(próximamente)_

```bash
cd frontend
npm install
npm run dev
```

---

## 🧪 Pruebas

Las pruebas de los endpoints se realizan con **Postman**.

Importa la colección de Postman incluida en la carpeta `/postman` del proyecto _(próximamente)_.

---

## 📄 Licencia

Este proyecto está bajo uso privado. Todos los derechos reservados.
