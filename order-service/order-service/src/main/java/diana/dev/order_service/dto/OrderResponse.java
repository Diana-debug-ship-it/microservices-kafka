package diana.dev.order_service.dto;

import diana.dev.order_service.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long productId,
        Integer quantity,
        OrderStatus status,
        Double totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
