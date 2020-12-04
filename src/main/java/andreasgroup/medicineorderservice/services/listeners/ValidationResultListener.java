package andreasgroup.medicineorderservice.services.listeners;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.services.MedicineOrderManager;
import andreasgroup.production.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidationResultListener {

    private final MedicineOrderManager medicineOrderManager;

    @JmsListener(destination = JmsConfiguration.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResult result){
        final UUID medicineOrderId = result.getOrderId();
        log.debug("Validation result for the order with Id: " + medicineOrderId);
        medicineOrderManager.processValidationResult(medicineOrderId, result.getIsValid());
    }
}
