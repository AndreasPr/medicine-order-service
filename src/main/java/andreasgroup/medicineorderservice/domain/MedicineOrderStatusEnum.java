package andreasgroup.medicineorderservice.domain;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public enum MedicineOrderStatusEnum {
    NEW,
    VALIDATED,
    VALIDATION_PENDING,
    VALIDATION_EXCEPTION,
    ALLOCATION_PENDING,
    ALLOCATED,
    ALLOCATION_EXCEPTION,
    CANCELLED,
    PENDING_INVENTORY,
    PICKED_UP,
    DELIVERED,
    DELIVERY_EXCEPTION
}
