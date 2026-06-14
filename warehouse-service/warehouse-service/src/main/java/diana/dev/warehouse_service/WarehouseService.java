package diana.dev.warehouse_service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final ProductRepository repository;
    private final OrderStatusKafkaProducer orderStatusKafkaProducer;

    public WarehouseService(ProductRepository repository, OrderStatusKafkaProducer orderStatusKafkaProducer) {
        this.repository = repository;
        this.orderStatusKafkaProducer = orderStatusKafkaProducer;
    }

    @Transactional
    public OrderStatusMessage processOrder(Order order) {
        ProductEntity productEntity = repository.findById(order.productId()).orElseThrow(
                () -> new EntityNotFoundException("Not found product by id "+order.productId()));

        if (productEntity.getInStock()<order.quantity()) {
            var message = new OrderStatusMessage(
                    order.orderId().toString(),
                    "REJECTED",
                    0.0
            );
            orderStatusKafkaProducer.sendOrderStatusMessageToKafka(message);
            return message;
        }

        int newStock = productEntity.getInStock() - order.quantity();
        productEntity.setInStock(newStock);
        repository.save(productEntity);

        double total = Math.round(productEntity.getPrice() * order.quantity() * 100.0) / 100.0;

        var message =  new OrderStatusMessage(
                order.orderId().toString(),
                "APPROVED",
                total
        );
        orderStatusKafkaProducer.sendOrderStatusMessageToKafka(message);
        return message;
    }
}
