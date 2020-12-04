package andreasgroup.medicineorderservice.statemachine.actions;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.services.MedicineOrderManagerImpl;
import andreasgroup.production.model.events.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationFailureAction implements Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> {

    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateContext) {
        String medicineOrderId = (String) stateContext.getMessage().getHeaders().get(MedicineOrderManagerImpl.ORDER_ID_HEADER);

        jmsTemplate.convertAndSend(JmsConfiguration.ALLOCATE_FAILURE_QUEUE,
                AllocationFailureEvent.builder()
                        .orderId(UUID.fromString(medicineOrderId))
                        .build());
        log.debug("Sent allocation failure message to the equivalent queue for order with id " + medicineOrderId);
    }
}
