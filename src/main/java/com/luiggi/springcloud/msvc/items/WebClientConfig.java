package com.luiggi.springcloud.msvc.items;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Toda clase de configuracion debe ser un componente de spring
@Configuration
public class WebClientConfig {

    // Inyectando el atributo url que viene de application.properties
    // @Value("${config.baseurl.endpoint.msvc-products}") // introduce el nombre de la configuración
    // private String url;

    // *******************************
    // Forma 1 manteniendo el contexto
    // *******************************

    /*
     * - WebClient se genera a partir de WebClient.Builder y este se genera de
     * forma automatica por Spring Boot
     * - este WebClient generado viene con el contexto original, con toda la
     * propagacion
     * - SOLUCIÓN de la pérdida de contexto
     */
    @Bean
    WebClient webClient(
            @Value("${config.baseurl.endpoint.msvc-products}") String url, // Inyectando el atributo url que viene de application.properties
            WebClient.Builder webClientBuilder,
            ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return webClientBuilder.baseUrl(url).filter(lbFunction).build();
    }

    // *******************************
    // Forma 2 sin mantener el contexto
    // *******************************

    /*
     * @Bean Lo que devuelva este metodo se convertira en un componente de spring y
     * lo registrara en el contenedor que luego podra ser usado/inyectado
     * 
     * @LoadBalanced: implementara balanceador de carga
     */
    /* @Bean
    @LoadBalanced
    WebClient.Builder webClient() {
    return WebClient.builder().baseUrl(url);
    } */
}
