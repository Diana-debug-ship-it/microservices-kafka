package diana.dev.warehouse_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusKafkaProducer.class);
    private final KafkaTemplate<String, OrderStatusMessage> kafkaTemplate;

    public OrderStatusKafkaProducer(KafkaTemplate<String, OrderStatusMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderStatusMessageToKafka(OrderStatusMessage orderStatusMessage) {
        kafkaTemplate.send("order-statuses", orderStatusMessage.orderId(), orderStatusMessage);
        log.info("Order sent to kafka: id={}", orderStatusMessage.orderId());
    }

}
