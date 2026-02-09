package com.luiggi.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.luiggi.libs.msvc.commons.entities.Product;
import com.luiggi.springcloud.msvc.items.models.Item;
import com.luiggi.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;



/**
 *  RefreshScope: permite actualizar los componentes/controladores, clases anotadas con
 * component, service, configuration, etc
 * y que además estemos inyectando configuraciones con @Value, Environment
 * RefreshScope actualiza y refresca el contexto, vuelve a inyectar estos valores
 * - se vuele a inicializar el componente con estos cambios en tiempo real sin 
 * reiniciar la aplicacion
 * - todo esto ocurre mediante una ruta url con actuator
 */
@RefreshScope
@RestController
public class ItemController {

    private final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService service;
    private final CircuitBreakerFactory cBreakerFactory;
    
    // Obtiene info del properties o de lo que este en el servidor de configuraciones
    @Value("${configuracion.texto}") 
    private String text;

    private final Environment env;

    // @Qualifier("nombre_del_bean/nombre_del_servicio_que_sera_inyectado_implementado") : el nombre debe tener la primera letra en minuscula
    // podemos tomar ItemServiceWebClient o ItemServiceFeign, en esta caso elegimos ItemServiceWebClient
    public ItemController(@Qualifier("itemServiceFeign") ItemService service,
        CircuitBreakerFactory cBreakerFactory,
        Environment env) {
        this.cBreakerFactory = cBreakerFactory;
        this.service = service;
        this.env = env;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("text", text);
        json.put("port", port);
        logger.info(port);
        logger.info(text);

        if(env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equals("dev")){
            json.put("autor.nombre", env.getProperty("configuracion.autor.nombre"));
            json.put("autor.email", env.getProperty(("configuracion.autor.email")));
        }

        return ResponseEntity.ok(json);
    }
    

    @GetMapping
    public List<Item> list(
        @RequestParam(name = "name", required = false) String name,
        @RequestHeader(name = "token-request", required = false) String token
    ) {
        logger.info("Llamada a metodo del controller ItemController::list()");
        logger.info("Request Parameter: {}", name);
        logger.info("Token: {}", token);
        return service.findAll();
    }

    // ********* FORMA 1 - para implementar circuit breaker
    // Esta forma funciona con el application.yml/application.properties o el archivo AppConfig en ese orden
    // para establecer los parámetros del circuit breaker
    // ResponseEntity<?>: retorna un generico
    /*
     * porque puede retornar un responseentity del tipo Item -> ResponseEntity<Item>
     * o del tipo map con el mensaje -> ResponseEntity<Map<String, String>> 
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        // Optional<Item> itemOptional = service.findById(id);
        Optional<Item> itemOptional = cBreakerFactory.create("items").run(
            () -> service.findById(id), // intenta hacer la consulta
            // si falla se ejecuta lo siguente, e: excepcion/error
            e -> {
                logger.error(e.getMessage());
                // también podemos usar otra api rest, pero en este caso no lo haremos
                Product product = new Product();
                product.setCreateAt(LocalDate.now());
                product.setId(1L);
                product.setName("Camara Sony");
                product.setPrice(500.00);
                return Optional.of(new Item(product, 5));
            }
        );


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

    // ********* FORMA 2 - para implementar circuit breaker
    // fallbackMethod: camino alternativo
    // name: nombre del corto circuito
    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<?> details2(@PathVariable Long id) {
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

    // Debe tener el mismo retorno que details2
    public ResponseEntity<?> getFallBackMethodProduct(Throwable e) {
        logger.error(e.getMessage());
        // también podemos usar otra api rest, pero en este caso no lo haremos
        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camara Sony");
        product.setPrice(500.00);
        return ResponseEntity.ok(new Item(product, 5));
    }

    
    // TimeLimiter: Indica el método al cual se le aplicará el tiempo de espera para cancelar y lanzar la excepcion
    @CircuitBreaker(name = "items", fallbackMethod = "getFallBackMethodProduct2")
    @TimeLimiter(name = "items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<?> details3(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> itemOptional = service.findById(id);

            if(itemOptional.isPresent()){
                return ResponseEntity.ok(itemOptional.get());
            }

            return ResponseEntity.status(404)
                .body(Collections.singletonMap(
                    "message", 
                    "No existe el producto en el microservicio msvc-products")
                );
        }); 
    }
    
    // Debe tener el mismo retorno que details3
    public CompletableFuture<?> getFallBackMethodProduct2(Throwable e) {
        return CompletableFuture.supplyAsync(() -> {
            logger.error(e.getMessage());
            // también podemos usar otra api rest, pero en este caso no lo haremos
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony");
            product.setPrice(500.00);
            return ResponseEntity.ok(new Item(product, 5));
        });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // alternativa a response entity
    public Product create(@RequestBody Product product) {
        logger.info("Product creando: {}", product);
        return service.save(product);
    }

    @ResponseStatus(HttpStatus.CREATED) // alternativa a response entity
    @PutMapping("/{id}")
    public Product update(@RequestBody Product product, @PathVariable Long id) {
        logger.info("Product actualizando: {}", product);
        return service.update(product, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // alternativa a response entity
    public void delete(@PathVariable Long id) {
        logger.info("Product eliminando por ID: {}", id);
        service.delete(id);
    }
}
