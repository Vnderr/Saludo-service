package com.devops.saludo.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SaludoServiceTest {
    private final SaludoService service = new SaludoService();

    @Test
    public void testGenerarSaludoConNombreNull() {
        String resultado = service.generarSaludo(null);
        assertEquals("Hola, Mundo!", resultado);
    }

    @Test
    public void testGenerarSaludoConNombreValido() {
        String resultado = service.generarSaludo("Juan");
        assertEquals("Hola, Juan!", resultado);
    }

    @Test
    public void testGetVersion() {
        String version = service.getVersion();
        assertEquals("1.0.0", version);
    }
}
