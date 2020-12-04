package andreasgroup.medicineorderservice.statemachine.actions;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.services.MedicineOrderManagerImpl;
import andreasgroup.medicineorderservice.web.mappers.MedicineOrderMapper;
import andreasgroup.production.model.events.ValidateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateOrderAction implements Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> {

    private final MedicineOrderRepository medicineOrderRepository;
    private final MedicineOrderMapper medicineOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateContext) {
        String medicineOrderId = (String) stateContext.getMessage().getHeaders().get(MedicineOrderManagerImpl.ORDER_ID_HEADER);
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(UUID.fromString(medicineOrderId));

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            jmsTemplate.convertAndSend(JmsConfiguration.VALIDATE_ORDER_QUEUE,
                    ValidateOrderRequest.builder()
                                .medicineOrder(medicineOrderMapper.medicineOrderToDto(medicineOrder))
                                .build());
        }, () -> log.error("Order Not Found. Id: " + medicineOrderId));

        log.debug("Sent Validation request to queue for order id " + medicineOrderId);
    }
}
