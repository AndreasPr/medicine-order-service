package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.CustomerRepository;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.web.mappers.MedicineOrderMapper;
import andreasgroup.production.model.MedicineOrderDto;
import andreasgroup.production.model.MedicineOrderPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class MedicineOrderServiceImpl implements MedicineOrderService {

    private final MedicineOrderRepository medicineOrderRepository;
    private final CustomerRepository customerRepository;
    private final MedicineOrderMapper medicineOrderMapper;
    private final MedicineOrderManager medicineOrderManager;

    @Override
    public MedicineOrderPagedList listOrders(UUID customerId, Pageable pageable) {

        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Page<MedicineOrder> medicineOrderPage = medicineOrderRepository.findAllByCustomer(customerOptional.get(), pageable);

            return new MedicineOrderPagedList(medicineOrderPage
                    .stream()
                    .map(medicineOrderMapper::medicineOrderToDto)
                    .collect(Collectors.toList()), PageRequest.of(
                    medicineOrderPage.getPageable().getPageNumber(),
                    medicineOrderPage.getPageable().getPageSize()),
                    medicineOrderPage.getTotalElements());
        }
        else {
            return null;
        }
    }

    @Transactional
    @Override
    public MedicineOrderDto placeOrder(UUID customerId, MedicineOrderDto medicineOrderDto) {

        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            MedicineOrder medicineOrder = medicineOrderMapper.dtoToMedicineOrder(medicineOrderDto);
            medicineOrder.setId(null);
            medicineOrder.setCustomer(customerOptional.get());
            medicineOrder.setOrderStatus(MedicineOrderStatusEnum.NEW);

            medicineOrder.getMedicineOrderLines().forEach(line -> line.setMedicineOrder(medicineOrder));

            MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);
            log.debug("Saved Medicine order: " + medicineOrder.getId());

            return medicineOrderMapper.medicineOrderToDto(savedMedicineOrder);
        }
        throw new RuntimeException("Customer was not found");
    }

    @Override
    public MedicineOrderDto getOrderById(UUID customerId, UUID orderId) {
        return medicineOrderMapper.medicineOrderToDto(getOrder(customerId, orderId));
    }

    @Override
    public void pickupOrder(UUID customerId, UUID orderId) {
        medicineOrderManager.medicineOrderPickedUp(orderId);
    }

    private MedicineOrder getOrder(UUID customerId, UUID orderId){
        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if(customerOptional.isPresent()){
            Optional<MedicineOrder> medicineOrderOptional = medicineOrderRepository.findById(orderId);

            if(medicineOrderOptional.isPresent()){
                MedicineOrder medicineOrder = medicineOrderOptional.get();

                if(medicineOrder.getCustomer().getId().equals(customerId)){
                    return medicineOrder;
                }
            }
            throw new RuntimeException("Medicine Order was not Found");
        }
        throw new RuntimeException("Customer was not found");
    }
}
