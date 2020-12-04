package andreasgroup.medicineorderservice.services.medicine;

import andreasgroup.production.model.MedicineDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * Created on 27/Nov/2020 to microservices-medicine-production
 */
@ConfigurationProperties(prefix = "andreas.production", ignoreUnknownFields = false)
@Service
public class MedicineServiceImpl implements MedicineService {

    public final static String MEDICINE_PATH_V1 = "/api/v1/medicine/";
    public final static String MEDICINE_UPC_PATH_V1 = "/api/v1/medicineUpc/";
    private final RestTemplate restTemplate;
    private String medicineServiceHost;

    public MedicineServiceImpl(RestTemplateBuilder restTemplateBuilder){
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<MedicineDto> getMedicineById(UUID uuid) {
        return Optional.of(restTemplate.getForObject(medicineServiceHost + MEDICINE_PATH_V1 + uuid.toString(), MedicineDto.class));
    }

    @Override
    public Optional<MedicineDto> getMedicineByUpc(String upc) {
        return Optional.of(restTemplate.getForObject(medicineServiceHost + MEDICINE_UPC_PATH_V1 + upc, MedicineDto.class));
    }

    public void setMedicineServiceHost(String medicineServiceHost){
        this.medicineServiceHost = medicineServiceHost;
    }
}
