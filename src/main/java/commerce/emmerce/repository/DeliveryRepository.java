package commerce.emmerce.repository;

import commerce.emmerce.domain.Delivery;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends R2dbcRepository<Delivery, Long> {
}
