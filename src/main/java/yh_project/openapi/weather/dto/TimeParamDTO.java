package yh_project.openapi.weather.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeParamDTO {
    private String date;
    private String time;
}
