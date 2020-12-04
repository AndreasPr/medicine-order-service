package andreasgroup.medicineorderservice.statemachine.actions;

import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.services.MedicineOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@Slf4j
public class ValidationFailureAction implements Action<MedicineOrderStatusEnum, MedicineOrderEventEnum>{
    @Override
    public void execute(StateContext<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateContext) {
        String medicineOrderId = (String) stateContext.getMessage().getHeaders().get(MedicineOrderManagerImpl.ORDER_ID_HEADER);
        log.error("Compensating transaction for failure validation with orderId: " + medicineOrderId);
    }
}
