package com.trinity.banca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacionApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banca API")
                        .version("1.1.0")
                        .description("Administración de clientes, cuentas de ahorros y corrientes, "
                                + "y transacciones: consignaciones, retiros y transferencias."));
    }
}
