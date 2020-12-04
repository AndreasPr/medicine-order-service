package andreasgroup.medicineorderservice.repositories;

import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface MedicineOrderRepository extends JpaRepository<MedicineOrder, UUID> {

    List<MedicineOrder> findAllByOrderStatus(MedicineOrderStatusEnum orderStatusEnum);
    Page<MedicineOrder> findAllByCustomer(Customer customer, Pageable pageable);
}
