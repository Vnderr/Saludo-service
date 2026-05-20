package com.devops.saludo.controller;

import com.devops.saludo.service.SaludoService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaludoController.class)
public class SaludoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaludoService saludoService; 

    @Test
    public void testEndpointInfoDeberiaRetornarOk() throws Exception {
        when(saludoService.getNombreServicio()).thenReturn("saludo-service");
        when(saludoService.getVersion()).thenReturn("1.0.0");

        mockMvc.perform(get("/info"))
                .andExpect(status().isOk());
    }

    @Test
    public void testEndpointBaseDeberiaRetornarOk() throws Exception {
        when(saludoService.generarSaludo(null)).thenReturn("Hola, Mundo!");
        when(saludoService.getVersion()).thenReturn("1.0.0");

        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}