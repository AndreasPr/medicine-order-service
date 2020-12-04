package andreasgroup.production.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
public class MedicinePagedList extends PageImpl<MedicineDto> implements Serializable {

    static final long serialVersionUID = -7596067853376368896L;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MedicinePagedList(@JsonProperty("content") List<MedicineDto> content,
                             @JsonProperty("number") int number,
                             @JsonProperty("size") int size,
                             @JsonProperty("totalElements") Long totalElements,
                             @JsonProperty("pageable") JsonNode pageable,
                             @JsonProperty("last") boolean last,
                             @JsonProperty("totalPages") int totalPages,
                             @JsonProperty("sort") JsonNode sort,
                             @JsonProperty("first") boolean first,
                             @JsonProperty("numberOfElements") int numberOfElements){
        super(content, PageRequest.of(number, size), totalElements);
    }

    public MedicinePagedList(List<MedicineDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public MedicinePagedList(List<MedicineDto> content) {
        super(content);
    }
}
