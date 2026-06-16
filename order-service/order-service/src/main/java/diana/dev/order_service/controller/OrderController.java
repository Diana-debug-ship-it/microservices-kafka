package diana.dev.order_service.controller;

import diana.dev.order_service.dto.Order;
import diana.dev.order_service.dto.OrderResponse;
import diana.dev.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid Order order) {
        log.info("Called createOrder: order={}", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable("id") Long id
    ) {
        log.info("Called getOrderById with id {}", id);
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Called getAllOrders");

        return ResponseEntity.status(HttpStatus.OK).body(orderService.findAllOrders());
    }

}
