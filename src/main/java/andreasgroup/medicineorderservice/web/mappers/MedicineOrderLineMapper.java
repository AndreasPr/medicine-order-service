package andreasgroup.medicineorderservice.web.mappers;

import andreasgroup.medicineorderservice.domain.MedicineOrderLine;
import andreasgroup.production.model.MedicineOrderLineDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Mapper(uses = {DateMapper.class})
@DecoratedWith(MedicineOrderLineMapperDecorator.class)
public interface MedicineOrderLineMapper {
    MedicineOrderLineDto medicineOrderLineToDto(MedicineOrderLine line);
    MedicineOrderLine dtoToMedicineOrderLine(MedicineOrderLineDto dto);
}
