package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.bootstrap.MedicineOrderBootstrap;
import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.repositories.CustomerRepository;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.production.model.MedicineOrderDto;
import andreasgroup.production.model.MedicineOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final MedicineOrderService medicineOrderService;
    private final MedicineOrderRepository medicineOrderRepository;
    private final List<String> medicineUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, MedicineOrderService medicineOrderService,
                              MedicineOrderRepository medicineOrderRepository){
        this.customerRepository = customerRepository;
        this.medicineOrderService = medicineOrderService;
        this.medicineOrderRepository = medicineOrderRepository;

        medicineUpcs.add(MedicineOrderBootstrap.MEDICINE_1_UPC);
        medicineUpcs.add(MedicineOrderBootstrap.MEDICINE_2_UPC);
        medicineUpcs.add(MedicineOrderBootstrap.MEDICINE_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 2000)
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(MedicineOrderBootstrap.TASTING_ROOM);

        if(customerList.size() == 1){
            doPlaceOrder(customerList.get(0));
        }
        else{
            log.error("Too many customers found or too few tasting room customer");
            customerList.forEach(customer -> log.debug(customer.toString()));
        }
    }

    private void doPlaceOrder(Customer customer){
        String medicineToOrder = getRandomMedicineUpc();

        MedicineOrderLineDto medicineOrderLine = MedicineOrderLineDto.builder()
                .upc(medicineToOrder)
                .orderQuantity(new Random().nextInt(6))
                .build();

        List<MedicineOrderLineDto> medicineOrderLineSet = new ArrayList<>();
        medicineOrderLineSet.add(medicineOrderLine);

        MedicineOrderDto medicineOrder = MedicineOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .medicineOrderLines(medicineOrderLineSet)
                .build();

        MedicineOrderDto savedOrder = medicineOrderService.placeOrder(customer.getId(), medicineOrder);
    }

    private String getRandomMedicineUpc(){
        return medicineUpcs.get(new Random().nextInt(medicineUpcs.size() - 0));
    }








}
