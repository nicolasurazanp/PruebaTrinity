package com.trinity.banca.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentacionApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void laEspecificacionOpenApiDescribeLaApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Banca API"))
                .andExpect(jsonPath("$.paths['/api/clientes']").exists())
                .andExpect(jsonPath("$.paths['/api/cuentas/{id}/cancelar']").exists())
                .andExpect(jsonPath("$.paths['/api/transacciones/transferencias']").exists());
    }

    @Test
    void laInterfazDeSwaggerEstaDisponible() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
