# LogiTrack Config Server

Servidor centralizado de configuración para el ecosistema de microservicios **LogiTrack**, desarrollado con **Spring Boot** y **Spring Cloud Config Server**.

La aplicación obtiene la configuración de los servicios desde un repositorio Git externo, permitiendo mantener separados los parámetros de configuración del código de cada microservicio.

## Características

- **Configuración centralizada:** sirve las propiedades de `shipment-service`, `courier-service` y `security-service` desde un repositorio Git.
- **Repositorio configurable:** la URI del repositorio se establece mediante la variable de entorno `CONFIG_GIT_URI`.
- **Clonado al iniciar:** `clone-on-start: true` hace que el servidor intente clonar el repositorio al arrancar.
- **Health check:** expone el endpoint de Actuator `/actuator/health`.
- **Contenerización:** incluye un `Dockerfile` multi-stage y ejecuta la aplicación con un usuario no privilegiado.
- **Gestión de secretos:** las credenciales y secretos deben inyectarse mediante variables de entorno, no almacenarse en el repositorio de configuración.

## Tecnologías

| Tecnología | Versión / detalle |
|---|---|
| Java | 25 |
| Spring Boot | 3.5.14 |
| Spring Cloud | 2025.0.3 |
| Spring Cloud Config Server | Gestionado por Spring Cloud BOM |
| Spring Boot Actuator | Health checks |
| Maven | Maven Wrapper 3.10.0 |
| Docker | Imagen de compilación JDK y ejecución JRE de Eclipse Temurin 25 |

## Arquitectura y funcionamiento

1. El servicio inicia como una aplicación Spring Boot con `@EnableConfigServer`.
2. En el arranque, intenta clonar el repositorio Git de configuración.
3. Los microservicios clientes solicitan sus propiedades al Config Server.
4. El Config Server devuelve la configuración correspondiente según el nombre de aplicación, el perfil y la etiqueta Git solicitados.

Repositorio de configuración predeterminado:

`https://github.com/TheMiggaCool/logitrack-config.git`

La rama predeterminada configurada es `main`.

> El repositorio Git de configuración debe existir y ser accesible desde el entorno donde se ejecuta el servidor. Si es privado, configura credenciales mediante variables de entorno.

## Requisitos

Para ejecutar el proyecto localmente:

- JDK 25.
- Acceso a Internet para descargar dependencias y, al iniciar, acceder al repositorio Git de configuración.
- Git, si se necesita operar manualmente sobre el repositorio.
- Docker, si se prefiere ejecutarlo como contenedor.

El Maven Wrapper está incluido, por lo que no es necesario instalar Maven globalmente.

## Configuración

### Variables de entorno

| Variable | Uso | Valor predeterminado |
|---|---|---|
| `CONFIG_GIT_URI` | URI del repositorio Git con las configuraciones | `https://github.com/TheMiggaCool/logitrack-config.git` |
| `SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME` | Usuario para acceder al repositorio privado | Sin valor predeterminado | - |
| `SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD` | Contraseña o token de acceso Git | Sin valor predeterminado | - |

Las variables de usuario y contraseña solo son necesarias si el repositorio requiere autenticación. No guardes tokens, contraseñas ni otros secretos en `application.yaml` ni en el repositorio Git.

### Puerto

El servidor escucha en el puerto **8888**.

## Ejecución local

Clona el repositorio y entra en su directorio:

```bash
git clone <URL_DEL_REPOSITORIO>
cd themiggacool-config-server
```

Ejecuta la aplicación en Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Para usar un repositorio de configuración distinto, define `CONFIG_GIT_URI` antes de iniciar la aplicación.

Windows PowerShell:

```powershell
$env:CONFIG_GIT_URI = "https://github.com/USUARIO/logitrack-config.git"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export CONFIG_GIT_URI="https://github.com/USUARIO/logitrack-config.git"
./mvnw spring-boot:run
```

## Construcción del JAR

Windows:

```powershell
.\mvnw.cmd clean package
java -jar target/config-server-0.0.1-SNAPSHOT.jar
```

Linux/macOS:

```bash
./mvnw clean package
java -jar target/config-server-0.0.1-SNAPSHOT.jar
```

El `Dockerfile` del proyecto compila la aplicación con las pruebas omitidas (`-DskipTests`). Para validar el proyecto antes de construirlo, ejecuta:

```bash
./mvnw test
```

En Windows, utiliza `.\mvnw.cmd test`.

## Ejecución con Docker

Construye la imagen desde la raíz del repositorio:

```bash
docker build -t logitrack-config-server .
```

Ejecuta el contenedor:

```bash
docker run --name logitrack-config-server   -p 8888:8888   -e CONFIG_GIT_URI="https://github.com/TheMiggaCool/logitrack-config.git"   logitrack-config-server
```

En PowerShell, puedes ejecutar el comando en una sola línea:

```powershell
docker run --name logitrack-config-server -p 8888:8888 -e CONFIG_GIT_URI="https://github.com/TheMiggaCool/logitrack-config.git" logitrack-config-server
```

Para un repositorio privado, pasa las variables de credenciales de forma segura al contenedor:

```bash
docker run --name logitrack-config-server   -p 8888:8888   -e CONFIG_GIT_URI="https://github.com/USUARIO/logitrack-config.git"   -e SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME="$CONFIG_GIT_USERNAME"   -e SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD="$CONFIG_GIT_PASSWORD"   logitrack-config-server
```

Evita escribir credenciales reales directamente en comandos que queden guardados en el historial de la terminal o en scripts versionados.

## Endpoints

Spring Cloud Config Server permite consultar la configuración mediante URLs con el formato:

```text
/{application}/{profile}
/{application}/{profile}/{label}
```

Ejemplos, con el servidor en ejecución local:

| Endpoint | Propósito |
|---|---|
| `http://localhost:8888/shipment-service/default` | Configuración predeterminada de `shipment-service` |
| `http://localhost:8888/courier-service/default` | Configuración predeterminada de `courier-service` |
| `http://localhost:8888/security-service/default` | Configuración predeterminada de `security-service` |
| `http://localhost:8888/actuator/health` | Estado de salud del servidor |

Los endpoints de configuración dependen de que existan archivos compatibles en el repositorio Git y de que el servicio pueda acceder a ellos.

## Estructura del proyecto

```text
themiggacool-config-server/
├── Dockerfile
├── .dockerignore
├── mvnw
├── mvnw.cmd
├── pom.xml
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties
└── src/
    └── main/
        ├── java/
        │   └── com/logitrack/configserver/
        │       └── ConfigServerApplication.java
        └── resources/
            └── application.yaml
```

## Health check y observabilidad

La aplicación incorpora Spring Boot Actuator y expone los endpoints `health` e `info` a través de HTTP. Para comprobar el estado:

```bash
curl http://localhost:8888/actuator/health
```

## Problemas frecuentes

- **La aplicación falla al iniciar:** verifica que `CONFIG_GIT_URI` sea correcta y que el repositorio esté disponible. Como `clone-on-start` está habilitado, un error al clonar puede impedir el arranque.
- **Error de autenticación Git:** comprueba las variables de entorno de credenciales. No incluyas tokens en el código.
- **Un microservicio no obtiene su configuración:** verifica el nombre de aplicación, el perfil solicitado, la rama/etiqueta Git y que los archivos de configuración existan en el repositorio remoto.
- **No responde el endpoint:** confirma que el contenedor esté en ejecución y que el puerto `8888` esté publicado.

## Seguridad

- No almacenes credenciales ni secretos en el repositorio de configuración.
- Utiliza variables de entorno o un gestor de secretos para las credenciales.
- Limita el acceso al Config Server a los componentes que lo necesiten.
- Considera proteger los endpoints de configuración con autenticación y transporte HTTPS en entornos productivos.

## Estado del proyecto

Este repositorio contiene el servidor de configuración centralizada de LogiTrack. La configuración concreta de cada microservicio se mantiene en el repositorio Git externo indicado por `CONFIG_GIT_URI`.

---

**Proyecto:** LogiTrack  
**Módulo:** Config Server
