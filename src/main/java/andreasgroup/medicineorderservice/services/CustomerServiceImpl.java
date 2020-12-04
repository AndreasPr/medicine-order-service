package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.repositories.CustomerRepository;
import andreasgroup.medicineorderservice.web.mappers.CustomerMapper;
import andreasgroup.production.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList listCustomers(Pageable pageable) {

        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return new CustomerPagedList(customerPage
                .stream()
                .map(customerMapper::customerToDto)
                .collect(Collectors.toList()),
                        PageRequest.of(
                            customerPage.getPageable().getPageNumber(),
                            customerPage.getPageable().getPageSize()),
                            customerPage.getTotalElements()
                        );
    }
}
