package microservices_crud.micro_restfull.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LibroResponse(
        String id,
        String titulo,
        String autor,
        String isbn,
        BigDecimal precio,
        Integer anioPublicacion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion) {
}
