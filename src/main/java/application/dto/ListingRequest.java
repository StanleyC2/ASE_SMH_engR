package application.dto;

import application.model.Neighborhood;
import lombok.Data;


//Purpose of dto is to avoid exposing the internal Entity logic
@Data
public class ListingRequest {
    private Neighborhood neighborhood;
    private Integer rent;
    private Integer bedrooms;
    private Integer bathrooms;
    private Boolean hasLift;
    private Boolean hasHeat;
    private Boolean hasAC;
    private String description;
}