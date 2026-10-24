package com.logitrack.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Servidor de configuración centralizada de LogiTrack.
 *
 * <p>Sirve las propiedades de shipment-service, courier-service y
 * security-service desde el repositorio Git {@code logitrack-config}.
 * Los secretos NO viven en ese repositorio: se inyectan como variables
 * de entorno en cada contenedor.</p>
 */
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
