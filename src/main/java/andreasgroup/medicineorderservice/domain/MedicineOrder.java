package andreasgroup.medicineorderservice.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
public class MedicineOrder extends BaseEntity{

    @Builder
    public MedicineOrder(UUID id, Long version, Timestamp createdDate, Timestamp lastModifiedDate, String customerRef,
                         Customer customer, Set<MedicineOrderLine> medicineOrderLines, MedicineOrderStatusEnum orderStatus,
                         String orderStatusCallbackUrl){
        super(id, version, createdDate, lastModifiedDate);
        this.customerRef = customerRef;
        this.customer = customer;
        this.medicineOrderLines = medicineOrderLines;
        this.orderStatus = orderStatus;
        this.orderStatusCallbackUrl = orderStatusCallbackUrl;
    }

    private String customerRef;

    @ManyToOne
    private Customer customer;

    @OneToMany(mappedBy = "medicineOrder", cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<MedicineOrderLine> medicineOrderLines;

    private MedicineOrderStatusEnum orderStatus = MedicineOrderStatusEnum.NEW;
    private String orderStatusCallbackUrl;
}
