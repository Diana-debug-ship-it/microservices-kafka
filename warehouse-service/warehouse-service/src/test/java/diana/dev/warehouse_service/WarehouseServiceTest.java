package diana.dev.warehouse_service;


import diana.dev.warehouse_service.dto.Order;
import diana.dev.warehouse_service.dto.OrderStatusMessage;
import diana.dev.warehouse_service.entity.ProductEntity;
import diana.dev.warehouse_service.kafka.producer.OrderStatusKafkaProducer;
import diana.dev.warehouse_service.repository.ProductRepository;
import diana.dev.warehouse_service.service.WarehouseService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @InjectMocks
    private WarehouseService warehouseService;

    @Mock
    private OrderStatusKafkaProducer orderStatusKafkaProducer;

    @Mock
    private ProductRepository repository;

    @Test
    void processOrder_ShouldThrowEntityNotFoundException_WhenProductNotFound() {

        Order order = new Order(4L, 2L, 10, "CREATED");

        when(repository.findById(2L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class,
                () -> warehouseService.processOrder(order));

        verify(repository, never()).save(any(ProductEntity.class));
        verifyNoInteractions(orderStatusKafkaProducer);
    }

    @Test
    void processOrder_ShouldReturnStatusRejected_WhenProductInStockIsNotEnough() {

        Order order = new Order(4L, 2L, 10, "CREATED");
        ProductEntity product = new ProductEntity(2L, "product", 5, 20.0);

        when(repository.findById(2L)).thenReturn(Optional.of(product));

        OrderStatusMessage msg = warehouseService.processOrder(order);
        Assertions.assertEquals("4", msg.orderId());
        Assertions.assertEquals("REJECTED", msg.status());
        Assertions.assertEquals(0.0, msg.totalPrice());

        verify(repository, never()).save(any(ProductEntity.class));
        verify(orderStatusKafkaProducer, times(1)).sendOrderStatusMessageToKafka(msg);
    }

    @Test
    void processOrder_ShouldReturnStatusApproved_WhenProductInStockIsEnough() {

        Order order = new Order(4L, 2L, 10, "CREATED");
        ProductEntity product = new ProductEntity(2L, "product", 20, 20.0);

        when(repository.findById(2L)).thenReturn(Optional.of(product));

        OrderStatusMessage msg = warehouseService.processOrder(order);
        Assertions.assertEquals("4", msg.orderId());
        Assertions.assertEquals("APPROVED", msg.status());
        Assertions.assertEquals(200.0, msg.totalPrice());

        verify(repository, times(1)).save(product);
        Assertions.assertEquals(10, product.getInStock());

        verify(orderStatusKafkaProducer, times(1)).sendOrderStatusMessageToKafka(msg);
    }
}
