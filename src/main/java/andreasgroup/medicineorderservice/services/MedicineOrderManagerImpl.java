package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderEventEnum;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.statemachine.MedicineOrderStateChangeInterceptor;
import andreasgroup.production.model.MedicineOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineOrderManagerImpl implements MedicineOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<MedicineOrderStatusEnum, MedicineOrderEventEnum> stateMachineFactory;
    private final MedicineOrderRepository medicineOrderRepository;
    private final MedicineOrderStateChangeInterceptor medicineOrderStateChangeInterceptor;

    @Transactional
    @Override
    public MedicineOrder newMedicineOrder(MedicineOrder medicineOrder) {
        medicineOrder.setId(null);
        medicineOrder.setOrderStatus(MedicineOrderStatusEnum.NEW);

        MedicineOrder savedMedicineOrder = medicineOrderRepository.saveAndFlush(medicineOrder);
        sendMedicineOrderEvent(savedMedicineOrder, MedicineOrderEventEnum.VALIDATE_ORDER);
        return savedMedicineOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID medicineOrderId, Boolean isValid) {
        log.debug("Process Validation Result for the medicine with Id: " + medicineOrderId + "Valid: " + isValid);

        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(medicineOrderId);

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            if(isValid){
                sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.VALIDATION_PASSED);

                //wait for status change
                awaitForStatus(medicineOrderId, MedicineOrderStatusEnum.VALIDATED);

                MedicineOrder validatedOrder = medicineOrderRepository.findById(medicineOrderId).get();
                sendMedicineOrderEvent(validatedOrder, MedicineOrderEventEnum.ALLOCATE_ORDER);
            }
            else{
                sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.VALIDATION_FAILED);
            }
        }, ()->log.error("Order was not found. Id: " + medicineOrderId));
    }

    @Override
    public void medicineOrderAllocationPassed(MedicineOrderDto medicineOrderDto) {
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(medicineOrderDto.getId());

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.ALLOCATION_SUCCESS);
            awaitForStatus(medicineOrder.getId(), MedicineOrderStatusEnum.ALLOCATED);
            updateAllocatedQty(medicineOrderDto);
        }, ()->log.error("Order Id was not found: " + medicineOrderDto.getId()));
    }

    @Override
    public void medicineOrderAllocationPendingInventory(MedicineOrderDto medicineOrderDto) {
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(medicineOrderDto.getId());

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.ALLOCATION_NO_INVENTORY);
            awaitForStatus(medicineOrder.getId(), MedicineOrderStatusEnum.PENDING_INVENTORY);
            updateAllocatedQty(medicineOrderDto);
        }, () -> log.error("Order Id was not found: " + medicineOrderDto.getId()));
    }

    @Override
    public void medicineOrderAllocationFailed(MedicineOrderDto medicineOrderDto) {
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(medicineOrderDto.getId());

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.ALLOCATION_FAILED);
        }, ()-> log.error("Order was not found with Id: " + medicineOrderDto.getId()));
    }

    @Override
    public void medicineOrderPickedUp(UUID id) {
        Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(id);

        medicineOrderOptional.ifPresentOrElse(medicineOrder -> {
            sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.MEDICINEORDER_PICKED_UP);
        }, ()->log.error("Order was not found with Id: " + id));
    }

    @Override
    public void cancelOrder(UUID id) {
        medicineOrderRepository.findById(id).ifPresentOrElse(medicineOrder -> {
            sendMedicineOrderEvent(medicineOrder, MedicineOrderEventEnum.CANCEL_ORDER);
        }, ()->log.error("Order was not found with Id: " + id));
    }

    private void updateAllocatedQty(MedicineOrderDto medicineOrderDto){
        Optional<MedicineOrder> allocatedOrderOptional = medicineOrderRepository.findById(medicineOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getMedicineOrderLines().forEach(medicineOrderLine->{
                medicineOrderDto.getMedicineOrderLines().forEach(medicineOrderLineDto -> {
                    if(medicineOrderLine.getId().equals(medicineOrderLineDto.getId())){
                        medicineOrderLine.setQuantityAllocated(medicineOrderLineDto.getQuantityAllocated());
                    }
                });
            });
            medicineOrderRepository.saveAndFlush(allocatedOrder);
        }, ()->log.error("Order Id was not found: " + medicineOrderDto.getId()));
    }

    private void sendMedicineOrderEvent(MedicineOrder medicineOrder, MedicineOrderEventEnum eventEnum){
        StateMachine<MedicineOrderStatusEnum, MedicineOrderEventEnum> sm = build(medicineOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, medicineOrder.getId().toString())
                .build();
        sm.sendEvent(msg);
    }

    private void awaitForStatus(UUID medicineOrderId, MedicineOrderStatusEnum statusEnum){

        AtomicInteger loopCount = new AtomicInteger(0);
        AtomicBoolean found = new AtomicBoolean(false);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop Retries exceeded");
            }

            medicineOrderRepository.findById(medicineOrderId).ifPresentOrElse(medicineOrder -> {
                if (medicineOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                    log.debug("Order Found");
                } else {
                    log.debug("Order Status was not Equal. Expected to have the following: " + statusEnum.name() +
                            ", but it wasFound: " + medicineOrder.getOrderStatus().name());
                }
            }, () -> { log.debug("Order Id was not Found"); });

            if (!found.get()) {
                try {
                    log.debug("Sleeping for retry");
                    Thread.sleep(100);
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    private StateMachine<MedicineOrderStatusEnum, MedicineOrderEventEnum> build(MedicineOrder medicineOrder){
        StateMachine<MedicineOrderStatusEnum, MedicineOrderEventEnum> sm = stateMachineFactory.getStateMachine(medicineOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(medicineOrderStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(medicineOrder.getOrderStatus(), null, null, null));
                });

        sm.start();

        return sm;
    }
}
