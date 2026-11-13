package microservices_crud.micro_restfull.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import microservices_crud.micro_restfull.dto.LibroRequest;
import microservices_crud.micro_restfull.dto.LibroResponse;
import microservices_crud.micro_restfull.exception.DatoDuplicadoException;
import microservices_crud.micro_restfull.exception.RecursoNoEncontradoException;
import microservices_crud.micro_restfull.model.Libro;
import microservices_crud.micro_restfull.repository.LibroRepository;

@Service
public class LibroService {

    private final LibroRepository repository;

    public LibroService(LibroRepository repository) {
        this.repository = repository;
    }

    public LibroResponse crear(LibroRequest request) {
        if (repository.existsByIsbn(request.isbn())) {
            throw new DatoDuplicadoException("Ya existe un libro con el ISBN " + request.isbn());
        }
        Libro libro = new Libro();
        aplicarDatos(libro, request);
        libro.setFechaCreacion(LocalDateTime.now());
        return aRespuesta(repository.save(libro));
    }

    public List<LibroResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    public LibroResponse obtenerPorId(String id) {
        return aRespuesta(buscarEntidad(id));
    }

    public LibroResponse actualizar(String id, LibroRequest request) {
        Libro libro = buscarEntidad(id);
        if (repository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new DatoDuplicadoException("Ya existe otro libro con el ISBN " + request.isbn());
        }
        aplicarDatos(libro, request);
        libro.setFechaActualizacion(LocalDateTime.now());
        return aRespuesta(repository.save(libro));
    }

    public void eliminar(String id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException(mensajeNoEncontrado(id));
        }
        repository.deleteById(id);
    }

    private Libro buscarEntidad(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(mensajeNoEncontrado(id)));
    }

    private String mensajeNoEncontrado(String id) {
        return "No se encontró el libro con id " + id;
    }

    private void aplicarDatos(Libro libro, LibroRequest request) {
        libro.setTitulo(request.titulo());
        libro.setAutor(request.autor());
        libro.setIsbn(request.isbn());
        libro.setPrecio(request.precio());
        libro.setAnioPublicacion(request.anioPublicacion());
    }

    private LibroResponse aRespuesta(Libro libro) {
        return new LibroResponse(libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getIsbn(),
                libro.getPrecio(), libro.getAnioPublicacion(), libro.getFechaCreacion(),
                libro.getFechaActualizacion());
    }
}
