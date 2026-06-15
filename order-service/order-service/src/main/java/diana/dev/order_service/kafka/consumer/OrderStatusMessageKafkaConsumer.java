package diana.dev.order_service.kafka.consumer;


import diana.dev.order_service.dto.OrderStatusMessage;
import diana.dev.order_service.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusMessageKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusMessageKafkaConsumer.class);
    private final OrderService orderService;

    public OrderStatusMessageKafkaConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "order-statuses")
    public void consumeOrderStatusMessage(ConsumerRecord<String, OrderStatusMessage> record) {
        log.info("Received Order Status Message: message={}, key={}, partition={}", record.value(), record.key(), record.partition());
        orderService.updateOrderStatus(record.value());
    }

}
