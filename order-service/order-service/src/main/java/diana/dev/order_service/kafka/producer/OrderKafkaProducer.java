package diana.dev.order_service.kafka.producer;

import diana.dev.order_service.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaProducer.class);
    private final KafkaTemplate<String, Order> kafkaTemplate;

    public OrderKafkaProducer(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderToKafka(Order order) {
        kafkaTemplate.send("orders", order.orderId().toString(), order);
        log.info("Order sent to kafka: id={}", order.orderId());
    }

}
