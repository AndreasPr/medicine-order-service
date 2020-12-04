package andreasgroup.medicineorderservice.services;

import andreasgroup.medicineorderservice.configuration.JmsConfiguration;
import andreasgroup.medicineorderservice.domain.Customer;
import andreasgroup.medicineorderservice.domain.MedicineOrder;
import andreasgroup.medicineorderservice.domain.MedicineOrderLine;
import andreasgroup.medicineorderservice.domain.MedicineOrderStatusEnum;
import andreasgroup.medicineorderservice.repositories.CustomerRepository;
import andreasgroup.medicineorderservice.repositories.MedicineOrderRepository;
import andreasgroup.medicineorderservice.services.medicine.MedicineServiceImpl;
import andreasgroup.production.model.MedicineDto;
import andreasgroup.production.model.events.AllocationFailureEvent;
import andreasgroup.production.model.events.DeallocateOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.jgroups.util.Util.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created on 01/Dec/2020 to microservices-medicine-production
 */
@ExtendWith(WireMockExtension.class)
@SpringBootTest
public class MedicineOrderManagerImplIT {

    @Autowired
    MedicineOrderManager medicineOrderManager;

    @Autowired
    MedicineOrderRepository medicineOrderRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    JmsTemplate jmsTemplate;

    Customer testCustomer;

    UUID medicineId = UUID.randomUUID();

    @TestConfiguration
    static class RestTemplateBuilderProvider {
        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer(){
            WireMockServer server = with(wireMockConfig().port(8083));
            server.start();
            return server;
        }
    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("Test Customer")
                .build());
    }

    @Test
    void testNewToAllocated() throws JsonProcessingException, InterruptedException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();

            assertEquals(MedicineOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            MedicineOrderLine line = foundOrder.getMedicineOrderLines().iterator().next();
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });

        MedicineOrder savedMedicineOrder2 = medicineOrderRepository.findById(savedMedicineOrder.getId()).get();

        assertNotNull(savedMedicineOrder2);
        assertEquals(MedicineOrderStatusEnum.ALLOCATED, savedMedicineOrder2.getOrderStatus());
        savedMedicineOrder2.getMedicineOrderLines().forEach(line -> {
            assertEquals(line.getOrderQuantity(), line.getQuantityAllocated());
        });
    }

    @Test
    void testFailedValidation() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();
        medicineOrder.setCustomerRef("fail-validation");

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();

            assertEquals(MedicineOrderStatusEnum.VALIDATION_EXCEPTION, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testNewToPickedUp() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        medicineOrderManager.medicineOrderPickedUp(savedMedicineOrder.getId());

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.PICKED_UP, foundOrder.getOrderStatus());
        });

        MedicineOrder pickedUpOrder = medicineOrderRepository.findById(savedMedicineOrder.getId()).get();

        assertEquals(MedicineOrderStatusEnum.PICKED_UP, pickedUpOrder.getOrderStatus());
    }

    @Test
    void testAllocationFailure() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();
        medicineOrder.setCustomerRef("fail-allocation");

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.ALLOCATION_EXCEPTION, foundOrder.getOrderStatus());
        });

        AllocationFailureEvent allocationFailureEvent = (AllocationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfiguration.ALLOCATE_FAILURE_QUEUE);

        assertNotNull(allocationFailureEvent);
        assertThat(allocationFailureEvent.getOrderId()).isEqualTo(savedMedicineOrder.getId());
    }

    @Test
    void testPartialAllocation() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();
        medicineOrder.setCustomerRef("partial-allocation");

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.PENDING_INVENTORY, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testValidationPendingToCancel() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();
        medicineOrder.setCustomerRef("dont-validate");

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.VALIDATION_PENDING, foundOrder.getOrderStatus());
        });

        medicineOrderManager.cancelOrder(savedMedicineOrder.getId());

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocationPendingToCancel() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();
        medicineOrder.setCustomerRef("dont-allocate");

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.ALLOCATION_PENDING, foundOrder.getOrderStatus());
        });

        medicineOrderManager.cancelOrder(savedMedicineOrder.getId());

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
        });
    }

    @Test
    void testAllocatedToCancel() throws JsonProcessingException {
        MedicineDto medicineDto = MedicineDto.builder().id(medicineId).upc("12345").build();

        wireMockServer.stubFor(get(MedicineServiceImpl.MEDICINE_UPC_PATH_V1 + "12345")
                .willReturn(okJson(objectMapper.writeValueAsString(medicineDto))));

        MedicineOrder medicineOrder = createMedicineOrder();

        MedicineOrder savedMedicineOrder = medicineOrderManager.newMedicineOrder(medicineOrder);

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.ALLOCATED, foundOrder.getOrderStatus());
        });

        medicineOrderManager.cancelOrder(savedMedicineOrder.getId());

        await().untilAsserted(() -> {
            MedicineOrder foundOrder = medicineOrderRepository.findById(medicineOrder.getId()).get();
            assertEquals(MedicineOrderStatusEnum.CANCELLED, foundOrder.getOrderStatus());
        });

        DeallocateOrderRequest deallocateOrderRequest = (DeallocateOrderRequest) jmsTemplate.receiveAndConvert(JmsConfiguration.DEALLOCATE_ORDER_QUEUE);

        assertNotNull(deallocateOrderRequest);
        assertThat(deallocateOrderRequest.getMedicineOrderDto().getId()).isEqualTo(savedMedicineOrder.getId());
    }

    public MedicineOrder createMedicineOrder(){
        MedicineOrder medicineOrder = MedicineOrder
                .builder()
                .customer(testCustomer)
                .build();

        Set<MedicineOrderLine> lines = new HashSet<>();
        lines.add(MedicineOrderLine.builder()
                .medicineId(medicineId)
                .upc("12345")
                .orderQuantity(1)
                .medicineOrder(medicineOrder)
                .build());

        medicineOrder.setMedicineOrderLines(lines);

        return medicineOrder;
    }
}
