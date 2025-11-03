package com.luiggi.springcloud.msvc.items;

import java.time.Duration;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

@Configuration
public class AppConfig {

    @Bean // para que se registre como componente de spring
    Customizer<Resilience4JCircuitBreakerFactory> customizerCircuitBreaker() {
        return (factory) -> factory.configureDefault(id -> {
            return new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(
                CircuitBreakerConfig
                .custom()
                .slidingWindowSize(10) // ventana de peticiones para ir al estado abierto
                .failureRateThreshold(50) // % de fallas para ir al estado abierto
                .waitDurationInOpenState(Duration.ofSeconds(10L)) // tiempo en el estado abierto
                .permittedNumberOfCallsInHalfOpenState(5) // ventana de llamadas en estado semi abierto
                .slowCallDurationThreshold(Duration.ofSeconds(2L))// tiempo en que va a ocurrir una llamada lenta
                .slowCallRateThreshold(50) // porcentaje de fallas de llamadas lentas para entrar al estado abierto
                .build()
            )
            .timeLimiterConfig( // configura el tiempo minimo  para que la peticion sea un error
                TimeLimiterConfig
                .custom()
                .timeoutDuration(Duration.ofSeconds(3L))
                .build()
            )
            .build();
        });
    }

}
