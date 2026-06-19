package diana.dev.order_service.service;

import diana.dev.order_service.dto.Order;
import diana.dev.order_service.dto.OrderResponse;
import diana.dev.order_service.dto.OrderStatusMessage;
import diana.dev.order_service.entity.OrderEntity;
import diana.dev.order_service.entity.OrderStatus;
import diana.dev.order_service.kafka.producer.OrderKafkaProducer;
import diana.dev.order_service.mapper.OrderMapper;
import diana.dev.order_service.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderKafkaProducer orderKafkaProducer;
    private final OrderRepository repository;
    private final OrderNotificationService notificationService;

    public OrderService(OrderKafkaProducer orderKafkaProducer, OrderRepository orderRepository, OrderNotificationService notificationService) {
        this.orderKafkaProducer = orderKafkaProducer;
        this.repository = orderRepository;
        this.notificationService = notificationService;
    }

    public OrderResponse saveOrder(Order orderToSave) {

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

        OrderEntity savedEntity = repository.save(entity);
        log.info("Order successfully saved to DB with ID: {}", savedEntity.getId());

        var orderToSend = OrderMapper.toResponse(savedEntity);

        orderKafkaProducer.sendOrderToKafka(OrderMapper.toDomainOrder(savedEntity));

        return orderToSend;
    }

    @Transactional
    public void updateOrderStatus(OrderStatusMessage msg) {
        Long id = Long.valueOf(msg.orderId());

        OrderEntity orderEntity = repository.findById(id)
                .orElseThrow(
                        () -> new EntityNotFoundException("Order not found with id " + id)
                );

        orderEntity.setStatus(OrderStatus.valueOf(msg.status()));
        orderEntity.setUpdatedAt(LocalDateTime.now());
        orderEntity.setTotalPrice(msg.totalPrice());

        OrderEntity entity = repository.save(orderEntity);
        OrderResponse responseDto = OrderMapper.toResponse(entity);
        log.info("Order {} successfully updated to status {}", id, msg.status());

        notificationService.sendOrderUpdate(responseDto);
    }

    private boolean checkQuantity(int quantity) {
        return quantity > 0;
    }

    public OrderResponse findOrderById(Long id) {

        OrderEntity entity = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Not found order by id " + id)
        );

        return OrderMapper.toResponse(entity);

    }

    public List<OrderResponse> findAllOrders() {

        List<OrderEntity> orders = repository.findAll();
        return orders.stream().map(OrderMapper::toResponse).toList();

    }

    public List<OrderResponse> findAllOrdersDESC() {

        List<OrderEntity> orders = repository.findAllByOrderByIdDesc();
        return orders.stream().map(OrderMapper::toResponse).toList();

    }
}
