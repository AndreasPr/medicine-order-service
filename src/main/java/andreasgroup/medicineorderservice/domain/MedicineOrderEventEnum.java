package andreasgroup.medicineorderservice.domain;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public enum MedicineOrderEventEnum {
    VALIDATE_ORDER,
    CANCEL_ORDER,
    VALIDATION_PASSED,
    VALIDATION_FAILED,
    ALLOCATE_ORDER,
    ALLOCATION_SUCCESS,
    ALLOCATION_NO_INVENTORY,
    ALLOCATION_FAILED,
    MEDICINEORDER_PICKED_UP
}
