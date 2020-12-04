package andreasgroup.production.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public class MedicineOrderPagedList extends PageImpl<MedicineOrderDto> {

    public MedicineOrderPagedList(List<MedicineOrderDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }
    public MedicineOrderPagedList(List<MedicineOrderDto> content){
        super(content);
    }
}
