package yh_project.openapi.weather.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class WeatherItemDTO implements Serializable {
    private String region;
    private String time;
    private String icon;
    private String temperature;
    private String sky;
    private String pty;
}
