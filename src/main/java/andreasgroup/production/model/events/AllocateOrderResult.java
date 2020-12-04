package andreasgroup.production.model.events;

import andreasgroup.production.model.MedicineOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateOrderResult {
    private MedicineOrderDto medicineOrderDto;
    private Boolean allocationError = false;
    private Boolean pendingInventory = false;
}
