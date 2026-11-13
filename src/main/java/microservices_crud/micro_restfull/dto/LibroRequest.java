package microservices_crud.micro_restfull.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LibroRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede exceder 150 caracteres")
        String titulo,

        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 100, message = "El autor no puede exceder 100 caracteres")
        String autor,

        @NotBlank(message = "El ISBN es obligatorio")
        @Pattern(regexp = "^(97[89])?\\d{9}[\\dXx]$",
                message = "El ISBN debe tener 10 o 13 caracteres válidos, sin guiones")
        String isbn,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 8 enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El año de publicación es obligatorio")
        @Min(value = 1000, message = "El año de publicación debe ser mayor o igual a 1000")
        @Max(value = 2100, message = "El año de publicación debe ser menor o igual a 2100")
        Integer anioPublicacion) {
}
