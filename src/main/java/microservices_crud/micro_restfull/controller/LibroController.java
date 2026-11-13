package microservices_crud.micro_restfull.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import microservices_crud.micro_restfull.dto.LibroRequest;
import microservices_crud.micro_restfull.dto.LibroResponse;
import microservices_crud.micro_restfull.service.LibroService;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "Libros", description = "Operaciones CRUD sobre el catálogo de libros")
public class LibroController {

    private final LibroService service;

    public LibroController(LibroService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Crear un libro")
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest request) {
        LibroResponse creado = service.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar todos los libros")
    public List<LibroResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un libro por id")
    public LibroResponse obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un libro")
    public LibroResponse actualizar(@PathVariable String id, @Valid @RequestBody LibroRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un libro")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
