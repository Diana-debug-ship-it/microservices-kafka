package diana.dev.warehouse_service;

public record OrderStatusMessage(
        String orderId,
        String status,
        Double totalPrice
) {
}
