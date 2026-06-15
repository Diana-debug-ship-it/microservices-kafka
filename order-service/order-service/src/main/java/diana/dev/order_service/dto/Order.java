package diana.dev.order_service.dto;

import diana.dev.order_service.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

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
