package microservices_crud.micro_restfull.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microservices_crud.micro_restfull.dto.LibroRequest;
import microservices_crud.micro_restfull.dto.LibroResponse;
import microservices_crud.micro_restfull.exception.DatoDuplicadoException;
import microservices_crud.micro_restfull.exception.RecursoNoEncontradoException;
import microservices_crud.micro_restfull.model.Libro;
import microservices_crud.micro_restfull.repository.LibroRepository;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    private static final String ID = "abc123";
    private static final String ISBN = "9780132350884";

    @Mock
    private LibroRepository repository;

    @InjectMocks
    private LibroService service;

    private LibroRequest solicitud() {
        return new LibroRequest("Clean Code", "Robert C. Martin", ISBN, new BigDecimal("450.00"), 2008);
    }

    private Libro libroExistente() {
        Libro libro = new Libro();
        libro.setId(ID);
        libro.setTitulo("Titulo anterior");
        libro.setAutor("Autor anterior");
        libro.setIsbn(ISBN);
        libro.setPrecio(new BigDecimal("100.00"));
        libro.setAnioPublicacion(2000);
        return libro;
    }

    @Test
    @DisplayName("crear: guarda el libro y devuelve la respuesta cuando el ISBN no existe")
    void crearExito() {
        when(repository.existsByIsbn(ISBN)).thenReturn(false);
        when(repository.save(any(Libro.class))).thenAnswer(inv -> {
            Libro l = inv.getArgument(0);
            l.setId(ID);
            return l;
        });

        LibroResponse respuesta = service.crear(solicitud());

        assertThat(respuesta.id()).isEqualTo(ID);
        assertThat(respuesta.titulo()).isEqualTo("Clean Code");
        assertThat(respuesta.isbn()).isEqualTo(ISBN);
        assertThat(respuesta.fechaCreacion()).isNotNull();
        assertThat(respuesta.fechaActualizacion()).isNull();

        ArgumentCaptor<Libro> captor = ArgumentCaptor.forClass(Libro.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAutor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("crear: lanza DatoDuplicadoException si el ISBN ya existe")
    void crearIsbnDuplicado() {
        when(repository.existsByIsbn(ISBN)).thenReturn(true);
        LibroRequest solicitud = solicitud();

        assertThatThrownBy(() -> service.crear(solicitud))
                .isInstanceOf(DatoDuplicadoException.class)
                .hasMessageContaining(ISBN);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("listar: devuelve todos los libros")
    void listar() {
        Libro otro = libroExistente();
        otro.setId("otro");
        when(repository.findAll()).thenReturn(List.of(libroExistente(), otro));

        List<LibroResponse> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(LibroResponse::id).containsExactly(ID, "otro");
    }

    @Test
    @DisplayName("listar: devuelve lista vacía si no hay libros")
    void listarVacio() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.listar()).isEmpty();
    }

    @Test
    @DisplayName("obtenerPorId: devuelve el libro cuando existe")
    void obtenerPorIdExito() {
        when(repository.findById(ID)).thenReturn(Optional.of(libroExistente()));

        LibroResponse respuesta = service.obtenerPorId(ID);

        assertThat(respuesta.id()).isEqualTo(ID);
        assertThat(respuesta.titulo()).isEqualTo("Titulo anterior");
    }

    @Test
    @DisplayName("obtenerPorId: lanza RecursoNoEncontradoException cuando no existe")
    void obtenerPorIdNoEncontrado() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(ID))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining(ID);
    }

    @Test
    @DisplayName("actualizar: modifica los datos y registra la fecha de actualización")
    void actualizarExito() {
        when(repository.findById(ID)).thenReturn(Optional.of(libroExistente()));
        when(repository.existsByIsbnAndIdNot(ISBN, ID)).thenReturn(false);
        when(repository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        LibroResponse respuesta = service.actualizar(ID, solicitud());

        assertThat(respuesta.titulo()).isEqualTo("Clean Code");
        assertThat(respuesta.precio()).isEqualByComparingTo("450.00");
        assertThat(respuesta.fechaActualizacion()).isNotNull();
    }

    @Test
    @DisplayName("actualizar: lanza RecursoNoEncontradoException si el libro no existe")
    void actualizarNoEncontrado() {
        when(repository.findById(ID)).thenReturn(Optional.empty());
        LibroRequest solicitud = solicitud();

        assertThatThrownBy(() -> service.actualizar(ID, solicitud))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar: lanza DatoDuplicadoException si el ISBN pertenece a otro libro")
    void actualizarIsbnDuplicado() {
        when(repository.findById(ID)).thenReturn(Optional.of(libroExistente()));
        when(repository.existsByIsbnAndIdNot(ISBN, ID)).thenReturn(true);
        LibroRequest solicitud = solicitud();

        assertThatThrownBy(() -> service.actualizar(ID, solicitud))
                .isInstanceOf(DatoDuplicadoException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("eliminar: elimina el libro cuando existe")
    void eliminarExito() {
        when(repository.existsById(ID)).thenReturn(true);

        service.eliminar(ID);

        verify(repository).deleteById(ID);
    }

    @Test
    @DisplayName("eliminar: lanza RecursoNoEncontradoException cuando no existe")
    void eliminarNoEncontrado() {
        when(repository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(ID))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(repository, never()).deleteById(any());
    }
}
