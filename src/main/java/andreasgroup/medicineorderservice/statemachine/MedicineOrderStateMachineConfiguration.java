package andreasgroup.medicineorderservice.statemachine;

import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class MedicineOrderStateMachineConfiguration extends StateMachineConfigurerAdapter<MedicineOrderStatusEnum, MedicineOrderEventEnum> {

    private final Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> validateOrderAction;
    private final Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> validationFailureAction;
    private final Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> allocateOrderAction;
    private final Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> allocationFailureAction;
    private final Action<MedicineOrderStatusEnum, MedicineOrderEventEnum> deallocateOrderAction;

    @Override
    public void configure(StateMachineStateConfigurer<MedicineOrderStatusEnum, MedicineOrderEventEnum> states) throws Exception {
        states.withStates()
                .initial(MedicineOrderStatusEnum.NEW)
                .states(EnumSet.allOf(MedicineOrderStatusEnum.class))
                .end(MedicineOrderStatusEnum.PICKED_UP)
                .end(MedicineOrderStatusEnum.DELIVERED)
                .end(MedicineOrderStatusEnum.CANCELLED)
                .end(MedicineOrderStatusEnum.DELIVERY_EXCEPTION)
                .end(MedicineOrderStatusEnum.VALIDATION_EXCEPTION)
                .end(MedicineOrderStatusEnum.ALLOCATION_EXCEPTION);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<MedicineOrderStatusEnum, MedicineOrderEventEnum> transitions) throws Exception {
        transitions.withExternal()
                    .source(MedicineOrderStatusEnum.NEW)
                    .target(MedicineOrderStatusEnum.VALIDATION_PENDING)
                    .event(MedicineOrderEventEnum.VALIDATE_ORDER)
                    .action(validateOrderAction)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.VALIDATION_PENDING)
                    .target(MedicineOrderStatusEnum.VALIDATED)
                    .event(MedicineOrderEventEnum.VALIDATION_PASSED)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.VALIDATION_PENDING)
                    .target(MedicineOrderStatusEnum.CANCELLED)
                    .event(MedicineOrderEventEnum.CANCEL_ORDER)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.VALIDATION_PENDING)
                    .target(MedicineOrderStatusEnum.VALIDATION_EXCEPTION)
                    .event(MedicineOrderEventEnum.VALIDATION_FAILED)
                    .action(validationFailureAction)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.VALIDATED)
                    .target(MedicineOrderStatusEnum.ALLOCATION_PENDING)
                    .event(MedicineOrderEventEnum.ALLOCATE_ORDER)
                    .action(allocateOrderAction)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.VALIDATED)
                    .target(MedicineOrderStatusEnum.CANCELLED)
                    .event(MedicineOrderEventEnum.CANCEL_ORDER)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATION_PENDING)
                    .target(MedicineOrderStatusEnum.ALLOCATED)
                    .event(MedicineOrderEventEnum.ALLOCATION_SUCCESS)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATION_PENDING)
                    .target(MedicineOrderStatusEnum.ALLOCATION_EXCEPTION)
                    .event(MedicineOrderEventEnum.ALLOCATION_FAILED)
                    .action(allocationFailureAction)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATION_PENDING)
                    .target(MedicineOrderStatusEnum.CANCELLED)
                    .event(MedicineOrderEventEnum.CANCEL_ORDER)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATION_PENDING)
                    .target(MedicineOrderStatusEnum.PENDING_INVENTORY)
                    .event(MedicineOrderEventEnum.ALLOCATION_NO_INVENTORY)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATED)
                    .target(MedicineOrderStatusEnum.PICKED_UP)
                    .event(MedicineOrderEventEnum.MEDICINEORDER_PICKED_UP)
                .and().withExternal()
                    .source(MedicineOrderStatusEnum.ALLOCATED)
                    .target(MedicineOrderStatusEnum.CANCELLED)
                    .event(MedicineOrderEventEnum.CANCEL_ORDER)
                    .action(deallocateOrderAction);
    }
}
