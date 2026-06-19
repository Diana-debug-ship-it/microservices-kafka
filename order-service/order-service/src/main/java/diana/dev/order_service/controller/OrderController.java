package diana.dev.order_service.controller;

import diana.dev.order_service.dto.Order;
import diana.dev.order_service.dto.OrderResponse;
import diana.dev.order_service.service.OrderNotificationService;
import diana.dev.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderNotificationService orderNotificationService;

    public OrderController(OrderService orderService, OrderNotificationService orderNotificationService) {
        this.orderService = orderService;
        this.orderNotificationService = orderNotificationService;
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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrders() {
        return orderNotificationService.createConnection();
    }

    @GetMapping("/ui")
    public org.springframework.web.servlet.ModelAndView getUiPage() {
        ModelAndView modelAndView =  new ModelAndView("index");

        java.util.List<OrderResponse> existingOrders = orderService.findAllOrdersDESC();

        // Передаем этот список внутрь HTML под именем "initialOrders"
        modelAndView.addObject("initialOrders", existingOrders);

        return modelAndView;

    }
}
