package andreasgroup.medicineorderservice.statemachine.actions;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.services.MedicineOrderManagerImpl;
import andreasgroup.medicineorderservice.web.mappers.MedicineOrderMapper;
import andreasgroup.production.model.events.AllocateOrderRequest;
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
@Slf4j
@RequiredArgsConstructor
@Component
public class AllocateOrderAction implements Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> {

    private final JmsTemplate jmsTemplate;
    private final MedicineOrderRepository medicineOrderRepository;
    private final MedicineOrderMapper medicineOrderMapper;

    @Override
    public void execute(StateContext<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateContext) {

        String medicineOrderId = (String) stateContext.getMessage().getHeaders().get(MedicineOrderManagerImpl.ORDER_ID_HEADER);
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(UUID.fromString(medicineOrderId));

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            jmsTemplate.convertAndSend(JmsConfiguration.ALLOCATE_ORDER_QUEUE,
                    AllocateOrderRequest.builder()
                                        .medicineOrderDto(medicineOrderMapper.medicineOrderToDto(medicineOrder))
                                        .build());

            log.debug("Sent allocation request for the order with id: " + medicineOrderId);
        }, ()->log.error("Medicine Order was not found"));
    }
}
