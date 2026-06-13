package diana.dev.order_service;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.time.LocalDateTime;

public record Order(
        @Null
        Long orderId,

        @NotNull
        Long productId,

        @NotNull
        Integer quantity,

        @Null
        OrderStatus status
) {
}
