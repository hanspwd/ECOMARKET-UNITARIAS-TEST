package com.ecomarket.analiticaservice.service;

import com.ecomarket.analiticaservice.dto.*;
import com.ecomarket.analiticaservice.exception.NoExisteEnBdException;
import com.ecomarket.analiticaservice.model.entity.AlertaSistema;
import com.ecomarket.analiticaservice.model.entity.MetricaDashboard;
import com.ecomarket.analiticaservice.model.entity.Reporte;
import com.ecomarket.analiticaservice.model.entity.RespaldoBaseDatos;
import com.ecomarket.analiticaservice.model.reference.EstadoReporte;
import com.ecomarket.analiticaservice.model.reference.EstadoRespaldo;
import com.ecomarket.analiticaservice.model.reference.NivelAlerta;
import com.ecomarket.analiticaservice.model.reference.TipoReporte;
import com.ecomarket.analiticaservice.repository.EstadoReporteRepository;
import com.ecomarket.analiticaservice.repository.EstadoRespaldoRepository;
import com.ecomarket.analiticaservice.repository.NivelAlertaRepository;
import com.ecomarket.analiticaservice.repository.TipoReporteRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnaliticaService {

    private static final Logger log = LoggerFactory.getLogger(AnaliticaService.class);

    private final ReporteDomainService reporteDomainService;
    private final AlertaDomainService alertaDomainService;
    private final RespaldoDomainService respaldoDomainService;
    private final MetricaDomainService metricaDomainService;
    private final TipoReporteRepository tipoReporteRepository;
    private final EstadoReporteRepository estadoReporteRepository;
    private final NivelAlertaRepository nivelAlertaRepository;
    private final EstadoRespaldoRepository estadoRespaldoRepository;
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Value("${microservicio.usuarios.url}")
    private String usuariosUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.pedidos.url}")
    private String pedidosUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.productos.url}")
    private String productosUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.inventario.url}")
    private String inventarioUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.pagos.url}")
    private String pagosUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.carrito.url}")
    private String carritoUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.soporte.url}")
    private String soporteUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.envios.url}")
    private String enviosUrl;

    private <T> List<T> fetchList(String baseUrl, String path, Class<T> elementClass) {
        try {
            ResponseEntity<List<T>> response = restTemplate.exchange(
                    baseUrl + path, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<T>>() {});
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.warn("Servicio {} no disponible: {}", path, e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Error al consultar {}: {}", path, e.getMessage());
            return null;
        }
    }

    private Reporte crearReporteBase(Long solicitanteId, Long tipoReporteId, Long estadoReporteId) {
        TipoReporte tipo = tipoReporteRepository.findById(tipoReporteId)
                .orElseThrow(() -> new NoExisteEnBdException("Tipo reporte ID " + tipoReporteId + " no encontrado."));
        EstadoReporte estado = estadoReporteRepository.findById(estadoReporteId)
                .orElseThrow(() -> new NoExisteEnBdException("Estado reporte ID " + estadoReporteId + " no encontrado."));
        return reporteDomainService.crearReporte(solicitanteId, tipo, estado);
    }

    // ====== REPORTES ======

    @Transactional(readOnly = true)
    public Reporte obtenerReporte(Long id) {
        return reporteDomainService.buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public List<Reporte> listarReportes() {
        return reporteDomainService.listarTodos();
    }

    @Transactional(readOnly = true)
    public List<Reporte> listarReportesPorSolicitante(Long solicitanteId) {
        return reporteDomainService.buscarPorSolicitante(solicitanteId);
    }

    @Transactional(readOnly = true)
    public List<Reporte> listarReportesPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return reporteDomainService.buscarPorRangoFechas(inicio, fin);
    }

    @Transactional
    public Reporte generarReporte(ReporteRequestDTO request) {
        return crearReporteBase(request.getSolicitanteId(), request.getTipoReporteId(), 1L);
    }

    @Transactional
    public Reporte generarReportePorRango(ReporteFechaRequestDTO request) {
        return crearReporteBase(request.getSolicitanteId(), request.getTipoReporteId(), 1L);
    }

    @Transactional
    public Reporte generarReporteUsuarios(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<ClienteDTO> clientes = fetchList(usuariosUrl, "/api/usuarios", ClienteDTO.class);
        if (clientes == null) {
            clientes = Collections.emptyList();
            noDisponibles.add("usuarios");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 1L, 2L);
        reporte.setUrlArchivoResultado("/reportes/usuarios/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(clientes.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReportePedidos(Long solicitanteId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<String> noDisponibles = new ArrayList<>();
        List<PedidoDTO> pedidos = fetchList(pedidosUrl, "/api/pedido", PedidoDTO.class);
        if (pedidos == null) {
            pedidos = Collections.emptyList();
            noDisponibles.add("pedidos");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 2L, 2L);
        reporte.setUrlArchivoResultado("/reportes/pedidos/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(pedidos.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReporteInventario(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<ProductoDTO> productos = fetchList(productosUrl, "/api/catalogo", ProductoDTO.class);
        if (productos == null) {
            productos = Collections.emptyList();
            noDisponibles.add("productos");
        }
        List<InventarioStockDTO> stocks = fetchList(inventarioUrl, "/api/inventario", InventarioStockDTO.class);
        if (stocks == null) {
            stocks = Collections.emptyList();
            noDisponibles.add("inventario");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 3L, 2L);
        reporte.setUrlArchivoResultado("/reportes/inventario/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(productos.size() + stocks.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReportePagos(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<PagoDTO> pagos = fetchList(pagosUrl, "/api/pagos", PagoDTO.class);
        if (pagos == null) {
            pagos = Collections.emptyList();
            noDisponibles.add("pagos");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 4L, 2L);
        reporte.setUrlArchivoResultado("/reportes/pagos/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(pagos.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReporteCarrito(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<CarritoDTO> items = fetchList(carritoUrl, "/api/carrito", CarritoDTO.class);
        if (items == null) {
            items = Collections.emptyList();
            noDisponibles.add("carrito");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 5L, 2L);
        reporte.setUrlArchivoResultado("/reportes/carrito/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(items.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReporteSoporte(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<TicketSoporteDTO> tickets = fetchList(soporteUrl, "/api/v1/soporte/tickets", TicketSoporteDTO.class);
        if (tickets == null) {
            tickets = Collections.emptyList();
            noDisponibles.add("soporte");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 6L, 2L);
        reporte.setUrlArchivoResultado("/reportes/soporte/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(tickets.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReporteEnvios(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        List<EnvioDTO> envios = fetchList(enviosUrl, "/api/v1/logistica-envios/envios", EnvioDTO.class);
        if (envios == null) {
            envios = Collections.emptyList();
            noDisponibles.add("envios");
        }
        Reporte reporte = crearReporteBase(solicitanteId, 7L, 2L);
        reporte.setUrlArchivoResultado("/reportes/envios/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(envios.size());
        return reporte;
    }

    @Transactional
    public Reporte generarReporteCompleto(Long solicitanteId) {
        List<String> noDisponibles = new ArrayList<>();
        int totalRegistros = 0;

        List<ClienteDTO> clientes = fetchList(usuariosUrl, "/api/usuarios", ClienteDTO.class);
        if (clientes == null) { noDisponibles.add("usuarios"); clientes = Collections.emptyList(); }
        totalRegistros += clientes.size();

        List<PedidoDTO> pedidos = fetchList(pedidosUrl, "/api/pedido", PedidoDTO.class);
        if (pedidos == null) { noDisponibles.add("pedidos"); pedidos = Collections.emptyList(); }
        totalRegistros += pedidos.size();

        List<ProductoDTO> productos = fetchList(productosUrl, "/api/catalogo", ProductoDTO.class);
        if (productos == null) { noDisponibles.add("productos"); productos = Collections.emptyList(); }
        totalRegistros += productos.size();

        List<InventarioStockDTO> stocks = fetchList(inventarioUrl, "/api/inventario", InventarioStockDTO.class);
        if (stocks == null) { noDisponibles.add("inventario"); stocks = Collections.emptyList(); }
        totalRegistros += stocks.size();

        List<PagoDTO> pagos = fetchList(pagosUrl, "/api/pagos", PagoDTO.class);
        if (pagos == null) { noDisponibles.add("pagos"); pagos = Collections.emptyList(); }
        totalRegistros += pagos.size();

        List<CarritoDTO> carrito = fetchList(carritoUrl, "/api/carrito", CarritoDTO.class);
        if (carrito == null) { noDisponibles.add("carrito"); carrito = Collections.emptyList(); }
        totalRegistros += carrito.size();

        List<TicketSoporteDTO> tickets = fetchList(soporteUrl, "/api/v1/soporte/tickets", TicketSoporteDTO.class);
        if (tickets == null) { noDisponibles.add("soporte"); tickets = Collections.emptyList(); }
        totalRegistros += tickets.size();

        List<EnvioDTO> envios = fetchList(enviosUrl, "/api/v1/logistica-envios/envios", EnvioDTO.class);
        if (envios == null) { noDisponibles.add("envios"); envios = Collections.emptyList(); }
        totalRegistros += envios.size();

        Reporte reporte = crearReporteBase(solicitanteId, 8L, 2L);
        reporte.setUrlArchivoResultado("/reportes/completo/" + reporte.getId());
        reporte.setTotalRegistrosProcesados(totalRegistros);
        return reporte;
    }

    // ====== ALERTAS ======

    @Transactional(readOnly = true)
    public AlertaSistema obtenerAlerta(Long id) {
        return alertaDomainService.buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public List<AlertaSistema> listarAlertas() {
        return alertaDomainService.listarTodas();
    }

    @Transactional(readOnly = true)
    public List<AlertaSistema> listarAlertasPorEstado(Boolean resuelta) {
        return alertaDomainService.buscarPorResuelta(resuelta);
    }

    @Transactional
    public AlertaSistema crearAlerta(AlertaRequestDTO request) {
        NivelAlerta nivel = nivelAlertaRepository.findById(request.getNivelAlertaId())
                .orElseThrow(() -> new NoExisteEnBdException("Nivel alerta con ID " + request.getNivelAlertaId() + " no encontrado."));
        return alertaDomainService.crear(nivel, request.getMensaje(), request.getModuloOrigen());
    }

    @Transactional
    public AlertaSistema resolverAlerta(Long id) {
        return alertaDomainService.marcarResuelta(id);
    }

    // ====== RESPALDOS ======

    @Transactional(readOnly = true)
    public RespaldoBaseDatos obtenerRespaldo(Long id) {
        return respaldoDomainService.buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public List<RespaldoBaseDatos> listarRespaldos() {
        return respaldoDomainService.listarTodos();
    }

    @Transactional
    public RespaldoBaseDatos ejecutarRespaldo(RespaldoRequestDTO request) {
        EstadoRespaldo estado = estadoRespaldoRepository.findById(request.getEstadoRespaldoId())
                .orElseThrow(() -> new NoExisteEnBdException("Estado respaldo con ID " + request.getEstadoRespaldoId() + " no encontrado."));
        return respaldoDomainService.crearRespaldo(estado, request.getTamanoMegabytes(), request.getRutaAlmacenamiento());
    }

    // ====== METRICAS ======

    @Transactional(readOnly = true)
    public MetricaDashboard obtenerMetricaPorId(Long id) {
        return metricaDomainService.buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public MetricaDashboard obtenerMetricaPorClave(String clave) {
        return metricaDomainService.buscarPorClave(clave);
    }

    @Transactional(readOnly = true)
    public List<MetricaDashboard> listarMetricas() {
        return metricaDomainService.listarTodas();
    }

    @Transactional
    public MetricaDashboard crearMetrica(MetricaRequestDTO request) {
        return metricaDomainService.crear(request.getClaveMetrica(), request.getValorNumerico(), request.getValorTexto());
    }

    @Transactional
    public void registrarAccion(AccionLogDTO logDto) {
        log.info("Acción registrada desde {}: {} - Usuario: {}", 
                logDto.getMicroservicio(), logDto.getAccion(), logDto.getUsuarioId());
        // Aquí se implementaría el guardado en BD
    }

    @Transactional
    public MetricaDashboard actualizarMetrica(Long id, MetricaRequestDTO request) {
        return metricaDomainService.actualizar(id, request.getValorNumerico(), request.getValorTexto());
    }
}
