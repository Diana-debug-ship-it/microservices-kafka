package diana.dev.order_service.repository;

import diana.dev.order_service.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // SQL: ORDER BY id DESC
    List<OrderEntity> findAllByOrderByIdDesc();
}
