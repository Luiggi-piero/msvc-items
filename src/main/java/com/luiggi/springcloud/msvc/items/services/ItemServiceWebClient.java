package com.luiggi.springcloud.msvc.items.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

// import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.web.reactive.function.client.WebClient.Builder;
// import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.luiggi.libs.msvc.commons.entities.Product;
import com.luiggi.springcloud.msvc.items.models.Item;

//@Primary
@Service
public class ItemServiceWebClient implements ItemService {

    private final WebClient client;

    public ItemServiceWebClient(WebClient client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return this.client
        .get()
        .accept(MediaType.APPLICATION_JSON) // aceptamos el tipo de contenido json
        .retrieve() // recive la respuesta, es una coleccion de objetos(lista de productos en webflux/ son fluxs)
        .bodyToFlux(Product.class) // tipo de dato de la respuesta, recibimos un lista de objetos (un flujo/flux/ es como un list de forma reactiva)
        .map(product -> {
            Random random = new Random();
            return new Item(product, random.nextInt(10) + 1);
        })
        .collectList()
        .block();
    }

    @Override
    public Optional<Item> findById(Long id) {
       Map<String, Long> params = new HashMap<>();
       params.put("id", id);

    //    try {
           return  Optional.of(
                client
                .get()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class) // recibimos un solo objeto, a comparacion de bodyToFlux
                .map(product -> {
                    Random random = new Random();
                    return new Item(product, random.nextInt(10) + 1);
                })
                .block()
            );
    //    } catch (WebClientResponseException e) {
    //     return Optional.empty();
    //    }

    }

    @Override
    public Product save(Product product) {
        return client
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(product)
        .retrieve() // envía la petición al servidor y prepara la respuesta para ser leída.
        .bodyToMono(Product.class)
        .block(); // se usa para esperar de manera síncrona el resultado de un Mono o Flux, detiene el flujo reactivo y bloquea el hilo actual hasta obtener el valor (o un error).
    }

    @Override
    public Product update(Product product, Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);

        return client
                .put()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON) // por defecto acepta json
                .contentType(MediaType.APPLICATION_JSON) // contentido de la solicitud
                .bodyValue(product)
                .retrieve()
                .bodyToMono(Product.class)
                .block();
    }

    @Override
    public void delete(Long id) {
        Map<String, Long> params = new HashMap<>();
        params.put("id", id);

        client.delete()
            .uri("/{id}", params)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }

}
