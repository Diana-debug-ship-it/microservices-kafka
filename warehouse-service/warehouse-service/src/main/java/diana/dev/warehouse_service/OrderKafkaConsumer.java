package diana.dev.warehouse_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaConsumer.class);


    @KafkaListener(topics = "orders")
    public void consumeOrder(Order order) {
        log.info("Received order: order={}", order);
    }
}
