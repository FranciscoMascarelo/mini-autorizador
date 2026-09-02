package com.teste.miniautorizador.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini Autorizador API")
                        .description("""
                    API para gerenciamento de cartões e autorização de transações.
                    
                    ## Funcionalidades
                    - Criação de cartões com saldo inicial de R$ 500,00
                    - Consulta de saldo
                    - Autorização de transações
                    
                    ## Regras de Autorização
                    1. Cartão deve existir
                    2. Senha deve ser válida
                    3. Saldo deve ser suficiente
                    """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("dev@exemplo.com")
                                .url("https://exemplo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local"),
                        new Server()
                                .url("https://api.exemplo.com")
                                .description("Servidor de Produção")
                ))
                .tags(Arrays.asList(
                        new Tag()
                                .name("Cartões")
                                .description("Operações relacionadas a cartões"),
                        new Tag()
                                .name("Transações")
                                .description("Operações relacionadas a transações")
                ));
    }
}