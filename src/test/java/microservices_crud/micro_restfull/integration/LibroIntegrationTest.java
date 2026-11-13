package microservices_crud.micro_restfull.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import microservices_crud.micro_restfull.repository.LibroRepository;

/**
 * Pruebas de integración: flujo completo HTTP -> controlador -> servicio -> MongoDB real (Testcontainers).
 * Requieren Docker en ejecución; si no hay Docker se omiten.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class LibroIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LibroRepository repository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        repository.deleteAll();
    }

    private String cuerpo(String isbn) {
        return """
                {
                  "titulo": "Clean Code",
                  "autor": "Robert C. Martin",
                  "isbn": "%s",
                  "precio": 450.00,
                  "anioPublicacion": 2008
                }
                """.formatted(isbn);
    }

    private String crearLibro(String isbn) throws Exception {
        String respuesta = mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo(isbn)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asText();
    }

    @Test
    @DisplayName("Flujo CRUD completo: crear, leer, listar, actualizar y eliminar")
    void flujoCrudCompleto() throws Exception {
        String id = crearLibro("9780132350884");
        assertThat(repository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/libros/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"))
                .andExpect(jsonPath("$.precio").value(450.00))
                .andExpect(jsonPath("$.fechaCreacion").isNotEmpty());

        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        String actualizado = cuerpo("9780132350884").replace("Clean Code", "Clean Code 2da Ed.");
        mockMvc.perform(put("/api/libros/" + id)
                        .contentType(MediaType.APPLICATION_JSON).content(actualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code 2da Ed."))
                .andExpect(jsonPath("$.fechaActualizacion").isNotEmpty());

        mockMvc.perform(delete("/api/libros/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/libros/" + id))
                .andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Crear con ISBN repetido -> 409")
    void isbnDuplicado() throws Exception {
        crearLibro("9780132350884");

        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("9780132350884")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Actualizar con el ISBN de otro libro -> 409")
    void actualizarConIsbnDeOtroLibro() throws Exception {
        crearLibro("9780132350884");
        String idSegundo = crearLibro("0132350882");

        mockMvc.perform(put("/api/libros/" + idSegundo)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("9780132350884")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Datos inválidos -> 400 y nada se guarda")
    void datosInvalidos() throws Exception {
        mockMvc.perform(post("/api/libros")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(org.hamcrest.Matchers.startsWith("isbn:")));

        assertThat(repository.count()).isZero();
    }

    @Test
    @DisplayName("Eliminar y actualizar un id inexistente -> 404")
    void idInexistente() throws Exception {
        mockMvc.perform(delete("/api/libros/no-existe"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/libros/no-existe")
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo("9780132350884")))
                .andExpect(status().isNotFound());
    }
}
