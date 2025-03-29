package com.luiggi.springcloud.msvc.items.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.luiggi.springcloud.msvc.items.models.Item;
import com.luiggi.springcloud.msvc.items.models.Product;

//@Primary
@Service
public class ItemServiceWebClient implements ItemService {

    private final WebClient.Builder client;

    public ItemServiceWebClient(Builder client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return this.client.build()
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

       try {
           return  Optional.of(
                client.build()
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
       } catch (WebClientResponseException e) {
        return Optional.empty();
       }

    }

}
