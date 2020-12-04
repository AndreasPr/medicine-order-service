package andreasgroup.medicineorderservice.web.mappers;

import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.production.model.MedicineOrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Mapper(uses = {DateMapper.class, MedicineOrderLineMapper.class})
public interface MedicineOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    MedicineOrderDto medicineOrderToDto(MedicineOrder medicineOrder);

    MedicineOrder dtoToMedicineOrder(MedicineOrderDto dto);
}
