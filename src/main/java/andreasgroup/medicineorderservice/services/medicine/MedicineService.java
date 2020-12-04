package andreasgroup.medicineorderservice.services.medicine;

import andreasgroup.production.model.MedicineDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface MedicineService {

    Optional<MedicineDto> getMedicineById(UUID uuid);
    Optional<MedicineDto> getMedicineByUpc(String upc);
}
