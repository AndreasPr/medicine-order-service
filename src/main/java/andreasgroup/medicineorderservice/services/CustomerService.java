package andreasgroup.medicineorderservice.services;

import andreasgroup.production.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public interface CustomerService {
    CustomerPagedList listCustomers(Pageable pageable);
}
