package microservices_crud.micro_restfull.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import microservices_crud.micro_restfull.dto.LibroRequest;
import microservices_crud.micro_restfull.dto.LibroResponse;
import microservices_crud.micro_restfull.exception.DatoDuplicadoException;
import microservices_crud.micro_restfull.exception.RecursoNoEncontradoException;
import microservices_crud.micro_restfull.service.LibroService;

@WebMvcTest(LibroController.class)
class LibroControllerTest {

    private static final String URL = "/api/libros";
    private static final String ID = "abc123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibroService service;

    private LibroRequest solicitud() {
        return new LibroRequest("Clean Code", "Robert C. Martin", "9780132350884",
                new BigDecimal("450.00"), 2008);
    }

    private LibroResponse respuesta() {
        return new LibroResponse(ID, "Clean Code", "Robert C. Martin", "9780132350884",
                new BigDecimal("450.00"), 2008, LocalDateTime.of(2026, 1, 15, 10, 30), null);
    }

    private String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    @Test
    @DisplayName("POST /api/libros -> 201 con Location y cuerpo serializado")
    void crear() throws Exception {
        when(service.crear(any(LibroRequest.class))).thenReturn(respuesta());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(solicitud())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(URL + "/" + ID)))
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.titulo").value("Clean Code"))
                .andExpect(jsonPath("$.precio").value(450.00));
    }

    @Test
    @DisplayName("POST /api/libros con datos vacíos -> 400 con detalle de campos")
    void crearInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("Error de validación en los datos enviados"))
                .andExpect(jsonPath("$.ruta").value(URL))
                .andExpect(jsonPath("$.detalles", hasSize(5)));
    }

    @Test
    @DisplayName("POST /api/libros con ISBN y precio inválidos -> 400")
    void crearFormatosInvalidos() throws Exception {
        LibroRequest invalido = new LibroRequest("Titulo", "Autor", "123-ABC",
                new BigDecimal("-5"), 2008);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasSize(2)));
    }

    @Test
    @DisplayName("POST /api/libros con JSON mal formado -> 400")
    void crearJsonMalFormado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{titulo:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición es inválido o está mal formado"));
    }

    @Test
    @DisplayName("POST /api/libros con ISBN repetido -> 409")
    void crearDuplicado() throws Exception {
        when(service.crear(any(LibroRequest.class)))
                .thenThrow(new DatoDuplicadoException("Ya existe un libro con el ISBN 9780132350884"));

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json(solicitud())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Ya existe un libro con el ISBN 9780132350884"));
    }

    @Test
    @DisplayName("GET /api/libros -> 200 con la lista")
    void listar() throws Exception {
        when(service.listar()).thenReturn(List.of(respuesta()));

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].autor").value("Robert C. Martin"));
    }

    @Test
    @DisplayName("GET /api/libros/{id} -> 200")
    void obtenerPorId() throws Exception {
        when(service.obtenerPorId(ID)).thenReturn(respuesta());

        mockMvc.perform(get(URL + "/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID));
    }

    @Test
    @DisplayName("GET /api/libros/{id} inexistente -> 404")
    void obtenerPorIdNoEncontrado() throws Exception {
        when(service.obtenerPorId("nope"))
                .thenThrow(new RecursoNoEncontradoException("No se encontró el libro con id nope"));

        mockMvc.perform(get(URL + "/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("No se encontró el libro con id nope"))
                .andExpect(jsonPath("$.ruta").value(URL + "/nope"));
    }

    @Test
    @DisplayName("PUT /api/libros/{id} -> 200")
    void actualizar() throws Exception {
        when(service.actualizar(eq(ID), any(LibroRequest.class))).thenReturn(respuesta());

        mockMvc.perform(put(URL + "/" + ID).contentType(MediaType.APPLICATION_JSON).content(json(solicitud())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"));
    }

    @Test
    @DisplayName("PUT /api/libros/{id} con datos inválidos -> 400")
    void actualizarInvalido() throws Exception {
        mockMvc.perform(put(URL + "/" + ID).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} -> 204")
    void eliminar() throws Exception {
        mockMvc.perform(delete(URL + "/" + ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} inexistente -> 404")
    void eliminarNoEncontrado() throws Exception {
        doThrow(new RecursoNoEncontradoException("No se encontró el libro con id nope"))
                .when(service).eliminar("nope");

        mockMvc.perform(delete(URL + "/nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/libros (sin id) -> 405")
    void metodoNoPermitido() throws Exception {
        mockMvc.perform(put(URL).contentType(MediaType.APPLICATION_JSON).content(json(solicitud())))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    @DisplayName("GET a una ruta inexistente -> 404")
    void rutaInexistente() throws Exception {
        mockMvc.perform(get("/api/inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("El recurso solicitado no existe"));
    }

    @Test
    @DisplayName("Error inesperado -> 500 con mensaje genérico")
    void errorInterno() throws Exception {
        when(service.listar()).thenThrow(new IllegalStateException("fallo inesperado"));

        mockMvc.perform(get(URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensaje").value("Error interno del servidor"));
    }
}
