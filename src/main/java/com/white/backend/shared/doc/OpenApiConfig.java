package com.white.backend.shared.doc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Host url")
        }
)
public class OpenApiConfig {
     @Bean
     public OpenAPI customOpenAPI(@Value("${spring.api.version}")String apiVersion) {

         return new OpenAPI()
                 .info(new Info()
                         .title("Core Backend")
                         .description("Core Backend")
                         .contact(new Contact().email("dungbui8198@gmail.com")
                                 .name("Hoang Dung White2077")
                                 .url("https://www.facebook.com/HoangKousui/"))
                         .version(apiVersion))
                 .components(new Components().addSecuritySchemes("token"
                         ,new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                 .scheme("bearer").bearerFormat("JWT")))
                 .addSecurityItem(new SecurityRequirement().addList("token"));

     }
     @Bean
     GroupedOpenApi publicApi() {

        return GroupedOpenApi.
                builder()
                .group("api")
                .packagesToScan("com.white.backend")
                .build();

    }
}
