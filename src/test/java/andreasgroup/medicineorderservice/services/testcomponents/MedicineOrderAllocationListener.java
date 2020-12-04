package andreasgroup.medicineorderservice.services.testcomponents;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.production.model.events.AllocateOrderRequest;
import andreasgroup.production.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created on 01/Dec/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfiguration.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg){
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();
        boolean pendingInventory = false;
        boolean allocationError = false;
        boolean sendResponse = true;

        //set allocation error
        if (request.getMedicineOrderDto().getCustomerRef() != null) {
            if (request.getMedicineOrderDto().getCustomerRef().equals("fail-allocation")){
                allocationError = true;
            }  else if (request.getMedicineOrderDto().getCustomerRef().equals("partial-allocation")) {
                pendingInventory = true;
            } else if (request.getMedicineOrderDto().getCustomerRef().equals("dont-allocate")){
                sendResponse = false;
            }
        }

        boolean finalPendingInventory = pendingInventory;

        request.getMedicineOrderDto().getMedicineOrderLines().forEach(medicineOrderLineDto -> {
            if (finalPendingInventory) {
                medicineOrderLineDto.setQuantityAllocated(medicineOrderLineDto.getOrderQuantity() - 1);
            } else {
                medicineOrderLineDto.setQuantityAllocated(medicineOrderLineDto.getOrderQuantity());
            }
        });

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfiguration.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResult.builder()
                            .medicineOrderDto(request.getMedicineOrderDto())
                            .pendingInventory(pendingInventory)
                            .allocationError(allocationError)
                            .build());
        }
    }
}
