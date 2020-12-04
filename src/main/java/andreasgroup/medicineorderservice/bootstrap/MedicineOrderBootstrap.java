package andreasgroup.medicineorderservice.bootstrap;

import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineOrderBootstrap implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    public static final String TASTING_ROOM = "Tasting Room";
    public static final String MEDICINE_1_UPC = "0631234200036";
    public static final String MEDICINE_2_UPC = "0631234300019";
    public static final String MEDICINE_3_UPC = "0083783375213";

    @Override
    public void run(String... args) throws Exception {
        loadCustomerData();
    }

    private void loadCustomerData(){
        if(customerRepository.findAllByCustomerNameLike(MedicineOrderBootstrap.TASTING_ROOM).size() == 0){
            Customer savedCustomer = customerRepository.saveAndFlush(
                    Customer.builder()
                            .customerName(TASTING_ROOM)
                            .apiKey(UUID.randomUUID())
                            .build()
            );
            log.debug("Tasting Room for the Customer with Id: " + savedCustomer.getId().toString());
        }
    }
}
