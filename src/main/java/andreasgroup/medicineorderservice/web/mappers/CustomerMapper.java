package andreasgroup.medicineorderservice.web.mappers;

import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.production.model.CustomerDto;
import org.mapstruct.Mapper;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {
    CustomerDto customerToDto(Customer customer);
    Customer dtoToCustomer(Customer dto);
}
