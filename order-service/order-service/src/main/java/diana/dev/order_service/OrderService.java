package diana.dev.order_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderKafkaProducer orderKafkaProducer;
    private final OrderRepository repository;

    public OrderService(OrderKafkaProducer orderKafkaProducer, OrderRepository orderRepository) {
        this.orderKafkaProducer = orderKafkaProducer;
        this.repository = orderRepository;
    }

    public Order saveOrder(Order orderToSave) {

        if (orderToSave.orderId()!=null) {
            throw new IllegalArgumentException("Id must be empty!");
        }

        if (orderToSave.status()!=null) {
            throw new IllegalArgumentException("Status must be empty!");
        }

        if (orderToSave.quantity()==null) {
            throw new IllegalArgumentException("Quantity must not be empty!");
        }

        if (!checkQuantity(orderToSave.quantity())) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        OrderEntity entity = OrderMapper.toOrderEntity(orderToSave);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setStatus(OrderStatus.CREATED);

        OrderEntity savedOrder = repository.save(entity);
        var orderToSend = OrderMapper.toDomainOrder(savedOrder);

        orderKafkaProducer.sendOrderToKafka(orderToSave);
        log.info("Order successfully save");

        return orderToSend;
    }

    private boolean checkQuantity(int quantity) {
        return quantity > 0;
    }
}
