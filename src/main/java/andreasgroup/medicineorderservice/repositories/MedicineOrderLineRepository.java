package andreasgroup.medicineorderservice.repositories;

import andreasgroup.medicineorderservice.domain.MedicineOrderLine;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface MedicineOrderLineRepository extends PagingAndSortingRepository<MedicineOrderLine, UUID> {
}
