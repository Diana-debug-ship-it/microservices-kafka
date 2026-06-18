package diana.dev.order_service;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import diana.dev.order_service.controller.OrderController;
import diana.dev.order_service.dto.Order;
import diana.dev.order_service.dto.OrderResponse;
import diana.dev.order_service.entity.OrderStatus;
import diana.dev.order_service.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllOrders_ShouldReturnEmptyList_WhenNoOrdersExists() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders_WhenOrdersExist() throws Exception {

        OrderResponse order1 = new OrderResponse(1L, 2L, 10, OrderStatus.CREATED, 5000.0, LocalDateTime.now(), null);
        OrderResponse order2 = new OrderResponse(2L, 5L, 10, OrderStatus.CREATED, 5000.0, LocalDateTime.now(), null);
        OrderResponse order3 = new OrderResponse(3L, 7L, 10, OrderStatus.CREATED, 5000.0, LocalDateTime.now(), null);

        when(orderService.findAllOrders()).thenReturn(Arrays.asList(order1, order2, order3));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[2].id").value(3L));
    }

    @Test
    void getOrderById_ShouldReturnOrderResponse_WhenOrderExists() throws Exception {
        Long id = 5L;
        OrderResponse order = new OrderResponse(id, 2L, 10, OrderStatus.CREATED, 5000.0, LocalDateTime.now(), null);

        when(orderService.findOrderById(id)).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrderById_ShouldThrowEntityNotFoundException_WhenOrderNotFound() throws Exception {
        Long id = 5L;

        doThrow(new EntityNotFoundException("Order not found")).when(orderService).findOrderById(id);

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_ShouldReturnOrderWithGeneratedId_WhenOrderIsValid() throws Exception {
        Long id = 5L;
        Order orderToSave = new Order(null, 2L, 10, null);
        OrderResponse savedOrder = new OrderResponse(id, 2L, 10, OrderStatus.CREATED, null, LocalDateTime.now(), null);

        when(orderService.saveOrder(orderToSave)).thenReturn(savedOrder);
        String orderJson = objectMapper.writeValueAsString(orderToSave);

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenIdNotNull() throws Exception {
        Order orderToSave = new Order(1L, 2L, 10, null);

        String orderJson = objectMapper.writeValueAsString(orderToSave);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenStatusIsNotNull() throws Exception {
        Order orderToSave = new Order(null, 2L, 10, OrderStatus.CREATED);

        String orderJson = objectMapper.writeValueAsString(orderToSave);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenQuantityIsNull() throws Exception {
        Order orderToSave = new Order(null, 2L, null, null);

        String orderJson = objectMapper.writeValueAsString(orderToSave);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrder_ShouldReturnBadRequest_WhenQuantityIs0() throws Exception {
        Order orderToSave = new Order(null, 2L, 0, null);

        String orderJson = objectMapper.writeValueAsString(orderToSave);

        when(orderService.saveOrder(orderToSave)).thenThrow(new IllegalArgumentException("Quantity must be greater than 0"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());
    }
}
