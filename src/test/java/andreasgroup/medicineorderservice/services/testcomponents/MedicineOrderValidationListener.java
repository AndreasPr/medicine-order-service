package andreasgroup.medicineorderservice.services.testcomponents;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.production.model.events.ValidateOrderRequest;
import andreasgroup.production.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created on 01/Dec/2020 to microservices-medicine-production
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MedicineOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfiguration.VALIDATE_ORDER_QUEUE)
    public void list(Message msg){
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        //condition to fail validation
        if (request.getMedicineOrder().getCustomerRef() != null) {
            if (request.getMedicineOrder().getCustomerRef().equals("fail-validation")){
                isValid = false;
            } else if (request.getMedicineOrder().getCustomerRef().equals("dont-validate")){
                sendResponse = false;
            }
        }

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfiguration.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(request.getMedicineOrder().getId())
                            .build());
        }
    }
}
