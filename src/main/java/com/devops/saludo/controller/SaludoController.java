package com.devops.saludo.controller; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devops.saludo.service.SaludoService;

import java.util.Map;

@RestController
public class SaludoController {

    @Autowired
    private final SaludoService saludoService;

    public SaludoController(SaludoService saludoService) {
        this.saludoService = saludoService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> holaMundo() {
        return ResponseEntity.ok(Map.of(
            "mensaje", saludoService.generarSaludo(null),
            "version", saludoService.getVersion()
        ));
    }

    @GetMapping("/saludo")
    public ResponseEntity<Map<String, String>> saludar(
            @RequestParam(defaultValue = "Mundo") String nombre) {
        return ResponseEntity.ok(Map.of(
            "mensaje", saludoService.generarSaludo(nombre),
            "version", saludoService.getVersion()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
            "servicio", saludoService.getNombreServicio(),
            "version", saludoService.getVersion(),
            "descripcion", "Microservicio de saludo para evaluacion DevOps",
            "tecnologia", "Spring Boot + Java "
        ));
    }
}