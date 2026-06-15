package diana.dev.warehouse_service.dto;

public record OrderStatusMessage(
        String orderId,
        String status,
        Double totalPrice
) {
}
