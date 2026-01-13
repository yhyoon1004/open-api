package yh_project.openapi.weather.dto;

import lombok.Data;
import yh_project.openapi.weather.WeatherStatus;

import java.io.Serializable;

@Data
public class WeatherItemDTO implements Serializable {
    private String region;      // 지역명 (예: 서울)
    private String time;        // 예보 기준 시간
    private String temperature; // 기온 (예: 24.5)
//    private String status;      // 요약 상태 (맑음, 구름많음, 비, 눈, 번개)
    private WeatherStatus status;   // 날씨 상태 (Enum)

    // 프론트엔드 표출용 한글 설명 (필요시 추가)
    public String getStatusDescription() {
        return status.getDescription();
    }
}
