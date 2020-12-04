package andreasgroup.medicineorderservice.web.mappers;

import andreasgroup.medicineorderservice.domain.MedicineOrderLine;
import andreasgroup.medicineorderservice.services.medicine.MedicineService;
import andreasgroup.production.model.MedicineDto;
import andreasgroup.production.model.MedicineOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public abstract class MedicineOrderLineMapperDecorator implements MedicineOrderLineMapper {

    private MedicineService medicineService;
    private MedicineOrderLineMapper medicineOrderLineMapper;

    @Autowired
    public void setMedicineService(MedicineService medicineService){
        this.medicineService = medicineService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setMedicineOrderLineMapper(MedicineOrderLineMapper medicineOrderLineMapper){
        this.medicineOrderLineMapper = medicineOrderLineMapper;
    }

    @Override
    public MedicineOrderLineDto medicineOrderLineToDto(MedicineOrderLine line){
        MedicineOrderLineDto orderLineDto = medicineOrderLineMapper.medicineOrderLineToDto(line);

        Optional<MedicineDto> medicineDtoOptional = medicineService.getMedicineByUpc(line.getUpc());

        medicineDtoOptional.ifPresent(medicineDto -> {
            orderLineDto.setMedicineName(medicineDto.getMedicineName());
            orderLineDto.setMedicineStyle(medicineDto.getMedicineStyle());
            orderLineDto.setPrice(medicineDto.getPrice());
            orderLineDto.setMedicineId(medicineDto.getId());
        });

        return orderLineDto;
    }

}
