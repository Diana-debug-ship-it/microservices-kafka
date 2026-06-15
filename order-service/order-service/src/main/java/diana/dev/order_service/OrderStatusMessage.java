package diana.dev.order_service;

public record OrderStatusMessage(
        String orderId,
        String status,
        Double totalPrice
) {
}
