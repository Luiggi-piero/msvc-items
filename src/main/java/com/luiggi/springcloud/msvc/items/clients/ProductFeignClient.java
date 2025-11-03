package com.luiggi.springcloud.msvc.items.clients;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.luiggi.libs.msvc.commons.entities.Product;


// Es un componente de spring al tener la anotación @EnableFeignClients en el archivo principal MsvcItemsApplication.java
// Esta interface seria como nuestro repositorio, pero en lugar de consultar una bd, consulta a otro servidor/app/msvc
// url="ip_o_nombre_de_dominio:puerto"
@FeignClient(name = "msvc-products")
public interface ProductFeignClient { 
    
    // este mapping (@GetMapping ) debe ser exacto al del microservicio
    /*
     * Ejemplo
     * - en el microservicio products debe haber también un @GetMapping en su controlador
     * - El método findAll se comunica con el msvc products a través de localhost:8001
     * - no importa el nombre del método solo el mapping, la ruta(localhost:8001), los parámetros del método y 
     * el tipo de retorno
     */
    @GetMapping 
    List<Product> findAll();

    @GetMapping("/{id}")
    Product details(@PathVariable Long id);

    @PostMapping
    public Product create(@RequestBody Product product);

    @PutMapping("/{id}")
    public Product update(@RequestBody Product product, @PathVariable Long id);

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id);

}
