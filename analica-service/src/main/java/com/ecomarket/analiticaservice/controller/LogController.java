package com.ecomarket.analiticaservice.controller;

import com.ecomarket.analiticaservice.dto.AccionLogDTO;
import com.ecomarket.analiticaservice.service.AnaliticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analitica/logs")
@RequiredArgsConstructor
public class LogController {

    private final AnaliticaService analiticaService;

    @PostMapping
    public ResponseEntity<Void> registrarAccion(@RequestBody AccionLogDTO logDto) {
        analiticaService.registrarAccion(logDto);
        return ResponseEntity.ok().build();
    }
}
