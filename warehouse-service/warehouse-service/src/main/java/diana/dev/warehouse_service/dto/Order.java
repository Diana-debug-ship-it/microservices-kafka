package diana.dev.warehouse_service.dto;

public record Order(
        Long orderId,
        Long productId,
        Integer quantity,
        String status
) {
}
