package com.ecomarket.envioservice.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.ecomarket.envioservice.client.AnaliticaMetricaClient;
import com.ecomarket.envioservice.client.SoporteNotificacionClient;
import com.ecomarket.envioservice.dto.ClienteDTO;
import com.ecomarket.envioservice.dto.PedidoDTO;
import com.ecomarket.envioservice.dto.TransportistaDTO;
import com.ecomarket.envioservice.exception.EnvioEstadoInvalidoException;
import com.ecomarket.envioservice.exception.NoExisteEnBdException;
import com.ecomarket.envioservice.exception.PedidoClienteIncompatibleException;
import com.ecomarket.envioservice.model.entity.Envio;
import com.ecomarket.envioservice.model.entity.HistorialEnvio;
import com.ecomarket.envioservice.model.entity.PuntoRetiro;
import com.ecomarket.envioservice.model.entity.RutaTransporte;
import com.ecomarket.envioservice.model.reference.EstadoEnvio;
import com.ecomarket.envioservice.model.reference.MetodoEnvio;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioDomainService envioDomainService;

    private final HistorialEnvioService historialEnvioService;

    private final RutaTransporteService rutaTransporteService;

    private final EstadoEnvioService estadoEnvioService;

    private final MetodoEnvioService metodoEnvioService;

    private final DireccionService direccionService;

    private final PuntoRetiroService puntoRetiroService;

    private final RestTemplate restTemplate;

    private final SoporteNotificacionClient soporteNotificacionClient;

    private final AnaliticaMetricaClient analiticaMetricaClient;

    @org.springframework.beans.factory.annotation.Value("${microservicio.usuarios.url}")
    private String usuariosUrl;

    @org.springframework.beans.factory.annotation.Value("${microservicio.pedidos.url}")
    private String pedidosUrl;

    private static final Logger log = LoggerFactory.getLogger(EnvioService.class);

    public Envio crearEnvio(Long pedidoId, Long clienteId, Long metodoEnvioId, Long direccionId) throws Exception {

        MetodoEnvio metodoEnvio = metodoEnvioService.findById(metodoEnvioId);
        direccionService.findById(direccionId);

        String urlCliente = usuariosUrl + "/api/usuarios/" + clienteId;
        try {
            @SuppressWarnings("unused")
            ClienteDTO cliente = restTemplate.getForObject(urlCliente, ClienteDTO.class);
        } catch (HttpClientErrorException.NotFound exNotFound) {
            throw new NoExisteEnBdException("No se puede crear el envio debido a que el id del cliente ingresado no existe en DB.");
        } catch (ResourceAccessException e) {
            log.warn("Servicio de clientes no disponible al validar cliente {}: {}", clienteId, e.getMessage());
            throw new NoExisteEnBdException("No se pudo validar el cliente debido a que el servicio de usuarios no esta disponible.");
        }

        String urlPedido = pedidosUrl + "/api/pedido/" + pedidoId;
        try {
            PedidoDTO pedido = restTemplate.getForObject(urlPedido, PedidoDTO.class);

            if (pedido != null && !pedido.getClienteId().equals(clienteId)) {
                throw new PedidoClienteIncompatibleException("No se puede crear el envio debido a que el ID del pedido no es compatible con el cliente asignado a ese pedido.");
            }
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NoExisteEnBdException("No se puede crear el envio debido a que el id del pedido ingresado no existe en DB.");
            }
            log.error("Error HTTP inesperado al validar pedido {}: {}", pedidoId, ex.getStatusCode());
            throw new Exception();
        } catch (ResourceAccessException e) {
            log.warn("Servicio de pedidos no disponible al validar pedido {}: {}", pedidoId, e.getMessage());
            throw new NoExisteEnBdException("No se pudo validar el pedido debido a que el servicio de pedidos no esta disponible.");
        }

        EstadoEnvio estadoInicial = estadoEnvioService.findById(1L);

        Envio envio = new Envio();
        envio.setPedidoId(pedidoId);
        envio.setClienteId(clienteId);
        envio.setMetodoEnvio(metodoEnvio);
        envio.setEstadoActual(estadoInicial);
        envio.setDireccionId(direccionId);
        envio.setCostoEnvio(calcularCosto(metodoEnvio));
        envio.setFechaCreacion(LocalDateTime.now());
        envio.setFechaEstimadaEntrega(calcularFechaEstimada(metodoEnvio));
        envio = envioDomainService.save(envio);

        HistorialEnvio historial = new HistorialEnvio();
        historial.setEnvioId(envio.getId());
        historial.setEstado(estadoInicial);
        historial.setFechaActualizacion(LocalDateTime.now());
        historial.setObservacion("Envio creado exitosamente.");
        historialEnvioService.save(historial);

        // Notificar al cliente via soporteservice
        soporteNotificacionClient.notificarCreacionEnvio(clienteId, pedidoId, envio.getId());

        // Registrar metrica en analiticaservice
        analiticaMetricaClient.registrarMetrica("envios.creados", 1.0, "Envio #" + envio.getId() + " creado para pedido #" + pedidoId);

        return envio;
    }

    public EstadoEnvio consultarEstadoEnvio(Long envioId) {
        Envio envio = envioDomainService.findById(envioId);
        return envio.getEstadoActual();
    }

    public HistorialEnvio actualizarEstado(Long envioId, Long nuevoEstadoId, String observacion) {
        Envio envio = envioDomainService.findById(envioId);
        EstadoEnvio nuevoEstado = estadoEnvioService.findById(nuevoEstadoId);

        envio.setEstadoActual(nuevoEstado);

        if (esEstadoFinal(nuevoEstadoId)) {
            envio.setFechaEntregaReal(LocalDateTime.now());
        }

        envioDomainService.save(envio);

        HistorialEnvio historial = new HistorialEnvio();
        historial.setEnvioId(envioId);
        historial.setEstado(nuevoEstado);
        historial.setFechaActualizacion(LocalDateTime.now());
        historial.setObservacion(observacion != null ? observacion : "Estado actualizado a: " + nuevoEstado.getNombre());
        HistorialEnvio saved = historialEnvioService.save(historial);

        // Registrar cambio de estado en analiticaservice
        analiticaMetricaClient.registrarMetrica("envios.estado.cambiado", 1.0,
                "Envio #" + envioId + " cambio a estado: " + nuevoEstado.getNombre());

        return saved;
    }

    public Boolean cancelarEnvio(Long envioId) {
        Envio envio = envioDomainService.findById(envioId);

        EstadoEnvio estadoActual = envio.getEstadoActual();
        if (esEstadoFinal(estadoActual.getId())) {
            throw new EnvioEstadoInvalidoException("No se puede cancelar el envio con id " + envioId + " porque ya se encuentra en un estado final.");
        }

        EstadoEnvio estadoCancelado = estadoEnvioService.findById(5L);

        envio.setEstadoActual(estadoCancelado);
        envioDomainService.save(envio);

        HistorialEnvio historial = new HistorialEnvio();
        historial.setEnvioId(envioId);
        historial.setEstado(estadoCancelado);
        historial.setFechaActualizacion(LocalDateTime.now());
        historial.setObservacion("Envio cancelado.");
        historialEnvioService.save(historial);

        return true;
    }

    public Envio registrarRecepcion(Long envioId, String firmaRecibe) {
        Envio envio = envioDomainService.findById(envioId);

        EstadoEnvio estadoEntregado = estadoEnvioService.findById(4L);

        envio.setEstadoActual(estadoEntregado);
        envio.setFechaEntregaReal(LocalDateTime.now());
        envioDomainService.save(envio);

        HistorialEnvio historial = new HistorialEnvio();
        historial.setEnvioId(envioId);
        historial.setEstado(estadoEntregado);
        historial.setFechaActualizacion(LocalDateTime.now());
        historial.setObservacion("Envio recibido. Firma: " + firmaRecibe);
        historialEnvioService.save(historial);

        return envio;
    }

    public Envio seleccionarPuntoRetiro(Long envioId, Long puntoRetiroId, String firmaRecibe) {
        Envio envio = envioDomainService.findById(envioId);
        PuntoRetiro puntoRetiro = puntoRetiroService.findById(puntoRetiroId);
        puntoRetiroService.verificarDisponibilidad(puntoRetiro);

        envio.setPuntoRetiro(puntoRetiro);
        envioDomainService.save(envio);

        EstadoEnvio estadoPuntoRetiro = estadoEnvioService.findById(3L);

        HistorialEnvio historial = new HistorialEnvio();
        historial.setEnvioId(envioId);
        historial.setEstado(estadoPuntoRetiro);
        historial.setFechaActualizacion(LocalDateTime.now());
        historial.setObservacion("Punto de retiro seleccionado: " + puntoRetiro.getNombre() + ". Firma: " + firmaRecibe);
        historialEnvioService.save(historial);

        return envio;
    }

    public RutaTransporte planificarRuta(Long transportistaId, List<Long> enviosIds) throws Exception {

        // (transportista no tiene microservicio propio; se usa mock-server como fallback)
        String urlTransportista = pedidosUrl + "/api/mock/transportistas/" + transportistaId;
        try {
            @SuppressWarnings("unused")
            TransportistaDTO transportista = restTemplate.getForObject(urlTransportista, TransportistaDTO.class);
        } catch (HttpClientErrorException.NotFound exNotFound) {
            throw new NoExisteEnBdException("No se puede planificar la ruta debido a que el id del transportista ingresado no existe en DB.");
        } catch (ResourceAccessException e) {
            log.warn("Servicio de transportistas no disponible al validar transportista {}: {}", transportistaId, e.getMessage());
            throw new NoExisteEnBdException("No se pudo validar el transportista debido a que el servicio no esta disponible.");
        }

        for (Long envioId : enviosIds) {
            envioDomainService.findById(envioId);
        }

        RutaTransporte ruta = new RutaTransporte();
        ruta.setTransportistaId(transportistaId);
        ruta.setFechaRuta(LocalDateTime.now());
        ruta.setCompletada(false);
        ruta.setEnviosIds(enviosIds);
        return rutaTransporteService.save(ruta);
    }

    public List<Envio> listarEnvios(Long clienteId, Long estadoId) {
        if (clienteId != null) {
            return envioDomainService.readByClienteId(clienteId);
        }
        if (estadoId != null) {
            return envioDomainService.readByEstadoId(estadoId);
        }
        return envioDomainService.readAll();
    }

    public Envio obtenerEnvioPorId(Long envioId) {
        return envioDomainService.findById(envioId);
    }

    public List<HistorialEnvio> obtenerHistorialEnvio(Long envioId) {
        envioDomainService.findById(envioId);
        return historialEnvioService.findHistorialByEnvioId(envioId);
    }

    private Double calcularCosto(MetodoEnvio metodoEnvio) {
        if ("PuntoRetiro".equalsIgnoreCase(metodoEnvio.getNombre())) {
            return 0.0;
        }
        return 5000.0;
    }

    private LocalDateTime calcularFechaEstimada(MetodoEnvio metodoEnvio) {
        if ("PuntoRetiro".equalsIgnoreCase(metodoEnvio.getNombre())) {
            return LocalDateTime.now().plusDays(2);
        }
        return LocalDateTime.now().plusDays(5);
    }

    private boolean esEstadoFinal(Long estadoId) {
        return estadoId == 4L || estadoId == 5L;
    }
}
