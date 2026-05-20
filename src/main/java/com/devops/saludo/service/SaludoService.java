package com.devops.saludo.service; 

import org.springframework.stereotype.Service;

@Service
public class SaludoService {
    private static final String VERSION = "1.0.0";

    public String generarSaludo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "Hola, Mundo!";
        }
        return "Hola, " + nombre.trim() + "!";
    }

    public String getVersion() {
        return VERSION;
    }

    public String getNombreServicio() {
        return "saludo-service";
    }
}