package diana.dev.order_service;

import diana.dev.order_service.dto.Order;
import diana.dev.order_service.dto.OrderResponse;
import diana.dev.order_service.dto.OrderStatusMessage;
import diana.dev.order_service.entity.OrderEntity;
import diana.dev.order_service.entity.OrderStatus;
import diana.dev.order_service.kafka.producer.OrderKafkaProducer;
import diana.dev.order_service.mapper.OrderMapper;
import diana.dev.order_service.repository.OrderRepository;
import diana.dev.order_service.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderKafkaProducer producer;

    @Test
    void saveOrder_ShouldReturnOrderResponse_WhenOrderIsValid() {

        Order order = new Order(null, 2L, 2, null);
        OrderEntity savedEntity = new OrderEntity(1L, 2L, 2, LocalDateTime.now(), null, OrderStatus.CREATED, null);

        when(repository.save(any(OrderEntity.class))).thenReturn(savedEntity);

        OrderResponse orderResponse = orderService.saveOrder(order);


        Assertions.assertNotNull(orderResponse.id());
        Assertions.assertEquals(OrderStatus.CREATED, orderResponse.status());
        Assertions.assertEquals(1L, orderResponse.id());
        verify(producer, times(1)).sendOrderToKafka(any(Order.class));
        verify(producer, times(1)).sendOrderToKafka(argThat(sentOrder ->
                sentOrder.quantity()==2 && sentOrder.status().equals(OrderStatus.CREATED)));
    }

    @Test
    void saveOrder_ShouldThrowIllegalArgumentException_WhenQuantityLessThan0() {

        Order order = new Order(null, 2L, -12, null);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(order));

    }

    @Test
    void saveOrder_ShouldThrowIllegalArgumentException_WhenQuantityIs0() {

        Order order = new Order(null, 2L, 0, null);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(order));

    }

    @Test
    void saveOrder_ShouldThrowIllegalArgumentException_WhenIdIsNotNull() {

        Order order = new Order(4L, 2L, 1, null);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(order));

    }

    @Test
    void saveOrder_ShouldThrowIllegalArgumentException_WhenStatusIsNotNull() {

        Order order = new Order(null, 2L, 1, OrderStatus.APPROVED);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(order));

    }

    @Test
    void saveOrder_ShouldThrowIllegalArgumentException_WhenQuantityIsNull() {

        Order order = new Order(null, 2L, null, null);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(order));

    }

    @Test
    void findById_ShouldReturnOrderResponse_WhenOrderExists() {

        Long id = 5L;
        OrderEntity orderEntity = new OrderEntity(5L, 2L, 10, LocalDateTime.now(), null, OrderStatus.CREATED, null);

        when(repository.findById(id)).thenReturn(Optional.of(orderEntity));

        OrderResponse response = orderService.findOrderById(id);

        Assertions.assertEquals(id,response.id());
    }

    @Test
    void findById_ShouldThrowEntityNotFoundException_WhenOrderDoesNotExist() {

        Long id = 5L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> orderService.findOrderById(id));
    }

    @Test
    void findAllOrders_ShouldReturnListOfOrders_WhenOrdersExist() {

        OrderEntity orderEntity1 = new OrderEntity(1L, 22L, 10, LocalDateTime.now(), null, OrderStatus.CREATED, null);
        OrderEntity orderEntity2 = new OrderEntity(12L, 24L, 10, LocalDateTime.now(), null, OrderStatus.CREATED, null);
        OrderEntity orderEntity3 = new OrderEntity(13L, 25L, 10, LocalDateTime.now(), null, OrderStatus.CREATED, null);

        when(repository.findAll()).thenReturn(List.of(orderEntity1, orderEntity2, orderEntity3));

        List<OrderResponse> orders = orderService.findAllOrders();

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals(1L, orders.get(0).id());
        Assertions.assertEquals(12L, orders.get(1).id());
        Assertions.assertEquals(13L, orders.get(2).id());
    }

    @Test
    void findAllOrders_ShouldReturnEmptyList_WhenNoOrdersExist() {

        when(repository.findAll()).thenReturn(List.of());

        List<OrderResponse> orders = orderService.findAllOrders();

        Assertions.assertEquals(0, orders.size());
    }

    @Test
    void updateOrderStatus_ShouldApproveAndSave_WhenOrderExists() {
        OrderStatusMessage msg = new OrderStatusMessage("1", "APPROVED", 5000.0);
        OrderEntity existingOrder = new OrderEntity(1L, 2L, 2, LocalDateTime.now(), null, OrderStatus.CREATED, null);

        when(repository.findById(1L)).thenReturn(Optional.of(existingOrder));

        orderService.updateOrderStatus(msg);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);

        verify(repository, times(1)).save(orderCaptor.capture());

        OrderEntity savedOrder = orderCaptor.getValue();

        Assertions.assertEquals(OrderStatus.APPROVED, savedOrder.getStatus());
        Assertions.assertEquals(5000.0, savedOrder.getTotalPrice());
        Assertions.assertNotNull(savedOrder.getUpdatedAt());
    }


    @Test
    void updateOrderStatus_ShouldRejectAndSave_WhenOrderExists() {
        OrderStatusMessage msg = new OrderStatusMessage("1", "REJECTED", 0.0);
        OrderEntity existingOrder = new OrderEntity(1L, 2L, 2, LocalDateTime.now(), null, OrderStatus.CREATED, null);

        when(repository.findById(1L)).thenReturn(Optional.of(existingOrder));

        orderService.updateOrderStatus(msg);

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);

        verify(repository, times(1)).save(orderCaptor.capture());

        OrderEntity savedOrder = orderCaptor.getValue();

        Assertions.assertEquals(OrderStatus.REJECTED, savedOrder.getStatus());
        Assertions.assertEquals(0.0, savedOrder.getTotalPrice());
        Assertions.assertNotNull(savedOrder.getUpdatedAt());
    }

    @Test
    void updateOrderStatus_ShouldThrowEntityNotFoundException_WhenOrderNotFound() {
        OrderStatusMessage msg = new OrderStatusMessage("99", "REJECTED", 0.0);

        when(repository.findById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(msg)
        );

        verify(repository, never()).save(any(OrderEntity.class));
    }

}
