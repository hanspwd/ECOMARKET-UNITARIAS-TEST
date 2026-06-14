package com.ecomarket.carritocompraservice.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecomarket.carritocompraservice.dto.ProductoClienteDTO;

@Component
public class CatalogoInventarioClient {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${microservicio.catalogo.url}")
    private String catalogoUrl;

    @Value("${microservicio.inventario.url}")
    private String inventarioUrl;

    // Consulta el producto por ID al catalogo-inventario-service
    public ProductoClienteDTO obtenerProducto(Long productoId) {
        try {
            String url = catalogoUrl + "/api/catalogo/" + productoId;
            return restTemplate.getForObject(url, ProductoClienteDTO.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Producto no encontrado en catalogo: " + productoId);
        }
    }

    // Verifica si hay stock disponible en el catalogo-inventario-service
    public boolean verificarDisponibilidad(Long productoId, Integer cantidad) {
        try {
            String url = inventarioUrl + "/api/inventario/disponibilidad/" + productoId
                    + "?cantidad=" + cantidad;
            Boolean resultado = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(resultado);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    // Reserva stock en el catalogo-inventario-service al iniciar checkout
    public boolean reservarStock(Long productoId, Integer cantidad) {
        try {
            String url = inventarioUrl + "/api/inventario/reservar/" + productoId
                    + "?cantidad=" + cantidad;
            Boolean resultado = restTemplate.postForObject(url, null, Boolean.class);
            return Boolean.TRUE.equals(resultado);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    // Libera stock en el catalogo-inventario-service si el carrito se vacía o cancela
    public boolean liberarStock(Long productoId, Integer cantidad) {
        try {
            String url = inventarioUrl + "/api/inventario/liberar/" + productoId
                    + "?cantidad=" + cantidad;
            Boolean resultado = restTemplate.postForObject(url, null, Boolean.class);
            return Boolean.TRUE.equals(resultado);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }
}
