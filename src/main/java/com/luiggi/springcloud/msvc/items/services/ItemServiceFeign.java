package com.luiggi.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.luiggi.springcloud.msvc.items.clients.ProductFeignClient;
import com.luiggi.springcloud.msvc.items.models.Item;
import com.luiggi.springcloud.msvc.items.models.Product;

import feign.FeignException;

@Service
public class ItemServiceFeign implements ItemService{

    @Autowired
    private ProductFeignClient client;

    @Override
    public List<Item> findAll() {
        
        // Forma 1
        return client.findAll().stream().map(product -> {
            Random random = new Random();
            // random.nextInt(10) : numero random de 0 a 9
            // random.nextInt(10) + 1 : numero random de 1 a 10
            return new Item(product, random.nextInt(10) + 1);
        }).collect(Collectors.toList());

        // Forma 2
        /* return client.findAll()
            .stream()
            .map(product -> new Item(product, new Random().nextInt(10) + 1))
            .collect(Collectors.toList());  */
    }

    @Override
    public Optional<Item> findById(Long id) {

        try {
            Product product = client.details(id);
            return Optional.of(new Item(product, new Random().nextInt(10) + 1));
        } catch (FeignException e) {
            return Optional.empty(); // si es nulo regresa un optianl vacio
        }
    }

}
