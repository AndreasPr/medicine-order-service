package andreasgroup.medicineorderservice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
public class MedicineOrderLine extends BaseEntity{

    @Builder
    public MedicineOrderLine(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate,
                             MedicineOrder medicineOrder, UUID medicineId, String upc, Integer orderQuantity,
                             Integer quantityAllocated) {
        super(id, version, createdDate, lastModifiedDate);
        this.medicineOrder = medicineOrder;
        this.medicineId = medicineId;
        this.upc = upc;
        this.orderQuantity = orderQuantity;
        this.quantityAllocated = quantityAllocated;
    }

    @ManyToOne
    private MedicineOrder medicineOrder;

    private UUID medicineId;
    private String upc;
    private Integer orderQuantity = 0;
    private Integer quantityAllocated = 0;

}
