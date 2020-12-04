package andreasgroup.medicineorderservice.web.controllers;

import andreasgroup.medicineorderservice.services.MedicineOrderService;
import andreasgroup.production.model.MedicineOrderDto;
import andreasgroup.production.model.MedicineOrderPagedList;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}/")
public class MedicineOrderController {

    private final MedicineOrderService medicineOrderService;
    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    public MedicineOrderController(MedicineOrderService medicineOrderService){
        this.medicineOrderService = medicineOrderService;
    }

    @GetMapping("orders")
    public MedicineOrderPagedList listOrders(@PathVariable("customerId") UUID customerId,
                                             @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                             @RequestParam(value = "pageSize", required = false) Integer pageSize){

        if(pageNumber == null || pageNumber < 0){
            pageNumber = DEFAULT_PAGE_NUMBER;
        }
        if(pageSize == null || pageSize < 1){
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return medicineOrderService.listOrders(customerId, PageRequest.of(pageNumber, pageSize));
    }

    @GetMapping("orders/{orderId}")
    public MedicineOrderDto getOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        return medicineOrderService.getOrderById(customerId, orderId);
    }

    @PostMapping("orders")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicineOrderDto placeOrder(@PathVariable("customerId") UUID customerId,
                                       @RequestBody MedicineOrderDto medicineOrderDto){
        return medicineOrderService.placeOrder(customerId, medicineOrderDto);
    }

    @PutMapping("orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pickupOrder(@PathVariable("customerId") UUID customerId, @PathVariable("orderId") UUID orderId){
        medicineOrderService.pickupOrder(customerId, orderId);
    }
}
