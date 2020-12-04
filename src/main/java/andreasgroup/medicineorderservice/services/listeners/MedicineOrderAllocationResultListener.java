package andreasgroup.medicineorderservice.services.listeners;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.services.MedicineOrderManager;
import andreasgroup.production.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineOrderAllocationResultListener {

    private final MedicineOrderManager medicineOrderManager;

    @JmsListener(destination = JmsConfiguration.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult result){
        if(!result.getAllocationError() && !result.getPendingInventory()){
            //allocated
            medicineOrderManager.medicineOrderAllocationPassed(result.getMedicineOrderDto());
        }
        else if(!result.getAllocationError() && result.getPendingInventory()){
            //pending inventory
            medicineOrderManager.medicineOrderAllocationPendingInventory(result.getMedicineOrderDto());
        }
        else if(result.getAllocationError()){
            //allocation error
            medicineOrderManager.medicineOrderAllocationFailed(result.getMedicineOrderDto());
        }
    }
}
