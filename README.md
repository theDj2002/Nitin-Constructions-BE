# Nitin Constructions — Java Backend (Spring Boot)

REST API built with **Spring Boot 3 + PostgreSQL (Aiven) + Cloudinary + JWT**.
This is a drop-in replacement for the Node.js backend — the React frontend works with both.

---

## 🗂 Project Structure

```
nitin-constructions-java/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/nitinconstructions/
    │   │   ├── NitinConstructionsApplication.java   ← Entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java              ← Spring Security + CORS
    │   │   │   └── CloudinaryConfig.java            ← Cloudinary bean
    │   │   ├── controller/
    │   │   │   ├── AuthController.java
    │   │   │   ├── ProjectController.java
    │   │   │   ├── UploadController.java
    │   │   │   └── HealthController.java
    │   │   ├── dto/
    │   │   │   └── Dtos.java                        ← All request/response classes
    │   │   ├── entity/
    │   │   │   ├── Project.java                     ← JPA entity (projects table)
    │   │   │   └── ProjectImage.java                ← JPA entity (project_images table)
    │   │   ├── exception/
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/
    │   │   │   ├── ProjectRepository.java
    │   │   │   └── ProjectImageRepository.java
    │   │   ├── security/
    │   │   │   ├── JwtUtil.java                     ← Token generate/validate
    │   │   │   └── JwtAuthFilter.java               ← Intercepts every request
    │   │   └── service/
    │   │       ├── AuthService.java
    │   │       ├── CloudinaryService.java
    │   │       └── ProjectService.java
    │   └── resources/
    │       ├── application.properties               ← Dev config (has your Aiven creds)
    │       └── application-prod.properties          ← Prod config (reads from env vars)
    └── test/
        ├── java/.../NitinConstructionsApplicationTests.java
        └── resources/application-test.properties   ← Uses H2 in-memory for tests
```

---

## ⚙️ Prerequisites

- **Java 17+** → [adoptium.net](https://adoptium.net)
- **Maven 3.8+** → [maven.apache.org](https://maven.apache.org)

Check your versions:
```bash
java -version
mvn -version
```

---

## 🚀 Run Locally

### 1. Fill in `application.properties`

Open `src/main/resources/application.properties` and update:

```properties
# Your Aiven PostgreSQL (already pre-filled from your screenshot)
spring.datasource.url=jdbc:postgresql://pg-262bd0cb-chandudibyajyotisahoo-e5b7.a.aivencloud.com:19906/defaultdb?sslmode=require
spring.datasource.username=avnadmin
spring.datasource.password=YOUR_ACTUAL_PASSWORD   ← paste real password here

# Change these
app.jwt.secret=any_random_string_min_32_characters_long
app.admin.password=nitin2024

# Cloudinary (from cloudinary.com dashboard)
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

### 2. Build & Run

```bash
cd nitin-constructions-java

# Run directly
mvn spring-boot:run

# OR build JAR first, then run
mvn clean package -DskipTests
java -jar target/nitin-constructions-api-1.0.0.jar
```

Server starts at: **http://localhost:8080**

### 3. Test it's working

```bash
curl http://localhost:8080/api/health
# → {"success":true,"data":{"status":"OK",...}}
```

---

## 🌐 API Endpoints

Base URL: `http://localhost:8080`

### Auth
| Method | Endpoint | Auth | Body / Notes |
|---|---|---|---|
| POST | `/api/auth/login` | ❌ | `{"password":"nitin2024"}` |
| GET | `/api/auth/verify` | ✅ | Validates Bearer token |

### Projects
| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| GET | `/api/projects` | ❌ | Returns visible only |
| GET | `/api/projects?admin=true` | ✅ | Returns all |
| GET | `/api/projects/{id}` | ❌ | Single project |
| POST | `/api/projects` | ✅ | Create project |
| PUT | `/api/projects/{id}` | ✅ | Update project |
| PATCH | `/api/projects/{id}/toggle` | ✅ | Toggle visibility |
| DELETE | `/api/projects/{id}` | ✅ | Delete + remove Cloudinary images |
| POST | `/api/projects/{id}/images` | ✅ | Add image by URL+publicId |
| POST | `/api/projects/{id}/images/upload` | ✅ | Upload file directly |
| DELETE | `/api/projects/{id}/images/{publicId}` | ✅ | Remove one image |

### Upload
| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/api/upload/single` | ✅ | `multipart/form-data`, field: `image` |
| POST | `/api/upload/multiple` | ✅ | `multipart/form-data`, field: `images` (max 10) |

### Health
| Method | Endpoint | Auth |
|---|---|---|
| GET | `/api/health` | ❌ |

---

## 🔗 Connect the React Frontend

In your React project (`client/src/utils/api.js`), change the `baseURL`:

```js
// Development — Java backend
const api = axios.create({
  baseURL: "http://localhost:8080/api",
});
```

Also update `client/vite.config.js` proxy target:
```js
proxy: {
  "/api": {
    target: "http://localhost:8080",   // ← was 5000
    changeOrigin: true,
  },
},
```

That's the only change needed — all API contracts are identical to the Node.js backend.

---

## 🚀 Production Deployment

### Option A — JAR on a VPS / server

```bash
mvn clean package -DskipTests
java -Dspring.profiles.active=prod \
     -DDB_URL="jdbc:postgresql://..." \
     -DDB_USERNAME="avnadmin" \
     -DDB_PASSWORD="yourpass" \
     -DJWT_SECRET="yourSecret" \
     -DADMIN_PASSWORD="strongPass" \
     -DCLOUDINARY_CLOUD_NAME="..." \
     -DCLOUDINARY_API_KEY="..." \
     -DCLOUDINARY_API_SECRET="..." \
     -DALLOWED_ORIGINS="https://yourdomain.com" \
     -jar target/nitin-constructions-api-1.0.0.jar
```

### Option B — Railway / Render

1. Push the project to GitHub
2. Create a new service pointing to this repo
3. Set all environment variables in the dashboard (same names as above)
4. Set start command: `java -Dspring.profiles.active=prod -jar target/nitin-constructions-api-1.0.0.jar`

---

## 🗄️ Database Tables (auto-created by Hibernate)

Hibernate creates these tables automatically on first run (`ddl-auto=update`):

**`projects`**
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL | Primary key |
| name | VARCHAR | Not null |
| type | VARCHAR | Not null |
| location | VARCHAR | Not null |
| description | TEXT | |
| year | INTEGER | Not null |
| is_visible | BOOLEAN | Default true |
| display_order | INTEGER | Default 0 |
| created_at | TIMESTAMP | Auto |
| updated_at | TIMESTAMP | Auto |

**`project_images`**
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL | Primary key |
| url | VARCHAR | Cloudinary URL |
| public_id | VARCHAR | Cloudinary public_id |
| caption | VARCHAR | Optional |
| project_id | BIGINT | FK → projects.id |

---

## 🔐 Security Notes

- The JWT token is signed with HMAC-SHA256 using your `app.jwt.secret`
- Token expiry is 8 hours (`28800000` ms)
- Admin password is stored in `.env` / `application.properties` — never in the DB
- All write endpoints require a valid `Authorization: Bearer <token>` header
- **⚠️ IMPORTANT**: Your Aiven credentials were visible in the screenshot you shared. Please reset your Aiven database password immediately from the Aiven dashboard.

---

*Nitin Constructions Pvt. Ltd. — Java Backend v1.0.0*
