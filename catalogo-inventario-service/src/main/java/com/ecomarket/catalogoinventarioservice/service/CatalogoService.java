package com.ecomarket.catalogoinventarioservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.ecomarket.catalogoinventarioservice.exception.NoExisteEnBdException;
import com.ecomarket.catalogoinventarioservice.exception.YaExisteEnBdException;
import com.ecomarket.catalogoinventarioservice.model.CategoriaProducto;
import com.ecomarket.catalogoinventarioservice.model.EspecificacionTecnica;
import com.ecomarket.catalogoinventarioservice.model.EstadoDisponibilidad;
import com.ecomarket.catalogoinventarioservice.model.Producto;
import com.ecomarket.catalogoinventarioservice.repository.CategoriaProductoRepository;
import com.ecomarket.catalogoinventarioservice.repository.EspecificacionTecnicaRepository;
import com.ecomarket.catalogoinventarioservice.repository.EstadoDisponibilidadRepository;
import com.ecomarket.catalogoinventarioservice.repository.ProductoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CatalogoService {
    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaRepository;
    private final EstadoDisponibilidadRepository estadoRepository;
    private final EspecificacionTecnicaRepository especificacionRepository;

    // --- Producto ---

    public List<Producto> navegarCatalogo(Long categoriaId) {
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new NoExisteEnBdException("Categoría no encontrada"));
        return productoRepository.findByCategoria(categoria);
    }

    public List<Producto> buscarCoincidenciaPorNombre(String nombreCoincidencia) {
        return productoRepository.findByNombreContainingIgnoreCase(nombreCoincidencia);
    }

    public Producto consultarDetalles(Long productoId) {
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new NoExisteEnBdException("Producto no encontrado: " + productoId));
    }

    public Producto agregarProducto(Producto nuevoProducto) {
        if (nuevoProducto.getCategoria() != null && nuevoProducto.getCategoria().getId() != null) {
            CategoriaProducto cat = categoriaRepository.findById(nuevoProducto.getCategoria().getId())
                    .orElseThrow(() -> new NoExisteEnBdException("Categoría no encontrada: " + nuevoProducto.getCategoria().getId()));
            nuevoProducto.setCategoria(cat);
        }
        if (nuevoProducto.getEstado() != null && nuevoProducto.getEstado().getId() != null) {
            EstadoDisponibilidad est = estadoRepository.findById(nuevoProducto.getEstado().getId())
                    .orElseThrow(() -> new NoExisteEnBdException("Estado no encontrado: " + nuevoProducto.getEstado().getId()));
            nuevoProducto.setEstado(est);
        }
        nuevoProducto.setFechaCreacion(LocalDateTime.now());
        try {
            return productoRepository.save(nuevoProducto);
        } catch (DataIntegrityViolationException e) {
            throw new YaExisteEnBdException("El producto con SKU '" + nuevoProducto.getSku() + "' ya existe.");
        }
    }

    public Producto editarProducto(Long productoId, Producto nuevosDatos) {
        Producto existente = consultarDetalles(productoId);
        existente.setSku(nuevosDatos.getSku());
        existente.setNombre(nuevosDatos.getNombre());
        existente.setDescripcion(nuevosDatos.getDescripcion());
        existente.setPrecioBase(nuevosDatos.getPrecioBase());
        if (nuevosDatos.getCategoria() != null && nuevosDatos.getCategoria().getId() != null) {
            CategoriaProducto cat = categoriaRepository.findById(nuevosDatos.getCategoria().getId())
                    .orElseThrow(() -> new NoExisteEnBdException("Categoría no encontrada: " + nuevosDatos.getCategoria().getId()));
            existente.setCategoria(cat);
        }
        if (nuevosDatos.getEstado() != null && nuevosDatos.getEstado().getId() != null) {
            EstadoDisponibilidad est = estadoRepository.findById(nuevosDatos.getEstado().getId())
                    .orElseThrow(() -> new NoExisteEnBdException("Estado no encontrado: " + nuevosDatos.getEstado().getId()));
            existente.setEstado(est);
        }
        existente.setImagenUrl(nuevosDatos.getImagenUrl());
        return productoRepository.save(existente);
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public boolean eliminarProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) return false;
        productoRepository.deleteById(productoId);
        return true;
    }

    // --- CategoriaProducto ---

    public List<CategoriaProducto> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public CategoriaProducto obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Categoría no encontrada: " + id));
    }

    public CategoriaProducto crearCategoria(CategoriaProducto categoria) {
        return categoriaRepository.save(categoria);
    }

    public CategoriaProducto editarCategoria(Long id, CategoriaProducto datos) {
        CategoriaProducto existente = obtenerCategoria(id);
        existente.setNombre(datos.getNombre());
        return categoriaRepository.save(existente);
    }

    public boolean eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) return false;
        categoriaRepository.deleteById(id);
        return true;
    }

    // --- EstadoDisponibilidad ---

    public List<EstadoDisponibilidad> listarEstados() {
        return estadoRepository.findAll();
    }

    public EstadoDisponibilidad obtenerEstado(Long id) {
        return estadoRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Estado no encontrado: " + id));
    }

    public EstadoDisponibilidad crearEstado(EstadoDisponibilidad estado) {
        return estadoRepository.save(estado);
    }

    public EstadoDisponibilidad editarEstado(Long id, EstadoDisponibilidad datos) {
        EstadoDisponibilidad existente = obtenerEstado(id);
        existente.setNombre(datos.getNombre());
        return estadoRepository.save(existente);
    }

    public boolean eliminarEstado(Long id) {
        if (!estadoRepository.existsById(id)) return false;
        estadoRepository.deleteById(id);
        return true;
    }

    // --- EspecificacionTecnica ---

    public List<EspecificacionTecnica> listarEspecificaciones() {
        return especificacionRepository.findAll();
    }

    public List<EspecificacionTecnica> listarEspecificacionesPorProducto(Long productoId) {
        return especificacionRepository.findAll().stream()
                .filter(e -> e.getProducto().getId().equals(productoId))
                .toList();
    }

    public EspecificacionTecnica obtenerEspecificacion(Long id) {
        return especificacionRepository.findById(id)
                .orElseThrow(() -> new NoExisteEnBdException("Especificación no encontrada: " + id));
    }

    public EspecificacionTecnica crearEspecificacion(EspecificacionTecnica especificacion) {
        return especificacionRepository.save(especificacion);
    }

    public EspecificacionTecnica editarEspecificacion(Long id, EspecificacionTecnica datos) {
        EspecificacionTecnica existente = obtenerEspecificacion(id);
        existente.setClave(datos.getClave());
        existente.setValor(datos.getValor());
        existente.setProducto(datos.getProducto());
        return especificacionRepository.save(existente);
    }

    public boolean eliminarEspecificacion(Long id) {
        if (!especificacionRepository.existsById(id)) return false;
        especificacionRepository.deleteById(id);
        return true;
    }
}
