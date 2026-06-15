package diana.dev.order_service.dto;

public record OrderStatusMessage(
        String orderId,
        String status,
        Double totalPrice
) {
}
