package diana.dev.order_service;

import java.time.LocalDateTime;

public class OrderMapper {

    public static Order toDomainOrder(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getStatus()
        );
    }

    public static OrderEntity toOrderEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setProductId(order.productId());
        entity.setQuantity(order.quantity());
        return entity;
    }

    public static OrderResponse toResponse(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getTotalPrice(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
