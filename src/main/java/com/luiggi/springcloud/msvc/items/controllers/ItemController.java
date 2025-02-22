package com.luiggi.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.luiggi.springcloud.msvc.items.models.Item;
import com.luiggi.springcloud.msvc.items.services.ItemService;

import feign.Response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
public class ItemController {

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<Item> list() {
        return service.findAll();
    }

    // ResponseEntity<?>: retorna un generico
    /*
     * porque puede retornar un responseentity del tipo Item -> ResponseEntity<Item>
     * o del tipo map con el mensaje -> ResponseEntity<Map<String, String>> 
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if(itemOptional.isPresent()){
            return ResponseEntity.ok(itemOptional.get());
        }
        // Forma 1
        //return ResponseEntity.notFound().build();

        // Forma 2
        return ResponseEntity.status(404)
            .body(Collections.singletonMap(
                "message", 
                "No existe el producto en el microservicio msvc-products")
            );
    }
    
    
}
