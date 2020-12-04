package andreasgroup.medicineorderservice.services;

import andreasgroup.production.model.MedicineOrderDto;
import andreasgroup.production.model.MedicineOrderPagedList;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface MedicineOrderService {

    MedicineOrderPagedList listOrders(UUID customerId, Pageable pageable);
    MedicineOrderDto placeOrder(UUID customerId, MedicineOrderDto medicineOrderDto);
    MedicineOrderDto getOrderById(UUID customerId, UUID orderId);
    void pickupOrder(UUID customerId, UUID orderId);
}
