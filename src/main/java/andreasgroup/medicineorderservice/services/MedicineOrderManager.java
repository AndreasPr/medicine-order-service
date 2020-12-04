package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.production.model.MedicineOrderDto;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface MedicineOrderManager {

    MedicineOrder newMedicineOrder(MedicineOrder medicineOrder);

    void processValidationResult(UUID medicineOrderId, Boolean isValid);

    void medicineOrderAllocationPassed(MedicineOrderDto medicineOrder);

    void medicineOrderAllocationPendingInventory(MedicineOrderDto medicineOrder);

    void medicineOrderAllocationFailed(MedicineOrderDto medicineOrder);

    void medicineOrderPickedUp(UUID id);

    void cancelOrder(UUID id);
}
