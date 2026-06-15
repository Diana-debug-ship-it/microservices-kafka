package diana.dev.warehouse_service.repository;

import diana.dev.warehouse_service.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
