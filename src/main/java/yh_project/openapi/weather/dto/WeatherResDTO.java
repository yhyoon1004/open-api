package yh_project.openapi.weather.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeatherResDTO {
    List<WeatherItemDTO> data;
}
