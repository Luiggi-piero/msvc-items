package com.luiggi.springcloud.msvc.items;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Toda clase de configuracion debe ser un componente de spring
@Configuration
public class WebClientConfig {

    // Inyectando el atributo url que viene de application.properties
    @Value("${config.baseurl.endpoint.msvc-products}") //introduce el nombre de la configuración
    private String url;

    /*
     * @Bean Lo que devuelva este metodo se convertira en un componente de spring y
     * lo registrara en el contenedor que luego podra ser usado/inyectado
     * 
     * @LoadBalanced: implementara balanceador de carga
     */
    @Bean
    @LoadBalanced
    WebClient.Builder webClient() {
        return WebClient.builder().baseUrl(url);
    }
}
