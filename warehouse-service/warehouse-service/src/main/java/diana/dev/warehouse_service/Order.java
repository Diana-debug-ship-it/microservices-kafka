package diana.dev.warehouse_service;

public record Order(
        Long orderId,
        Long productId,
        Integer quantity,
        String status
) {
}
