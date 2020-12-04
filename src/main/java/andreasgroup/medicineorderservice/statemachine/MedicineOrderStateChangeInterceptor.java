package andreasgroup.medicineorderservice.statemachine;

import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.services.MedicineOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<MedicineOrderStatusEnum, MedicineOrderEventEnum> {

    private final MedicineOrderRepository medicineOrderRepository;

    @Transactional
    @Override
    public void preStateChange(State<MedicineOrderStatusEnum, MedicineOrderEventEnum> state, Message<MedicineOrderEventEnum> message, Transition<MedicineOrderStatusEnum, MedicineOrderEventEnum> transition, StateMachine<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateMachine) {
        log.debug("PreState Change");

        Optional.ofNullable(message)
                .flatMap(msg->Optional.ofNullable((String) msg.getHeaders().getOrDefault(MedicineOrderManagerImpl.ORDER_ID_HEADER, " ")))
                .ifPresent(orderId->{
                    log.debug("Saving state for the order with Id: " + orderId + "and Status: " + state.getId());

                    MedicineOrder medicineOrder = medicineOrderRepository.getOne(UUID.fromString(orderId));
                    medicineOrder.setOrderStatus(state.getId());
                    medicineOrderRepository.saveAndFlush(medicineOrder);
                });
    }
}
