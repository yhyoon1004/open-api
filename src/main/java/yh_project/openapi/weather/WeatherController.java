package yh_project.openapi.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yh_project.openapi.cache.CacheManageService;
import yh_project.openapi.weather.dto.WeatherItemDTO;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    private final CacheManageService cacheManageService;

    @RequestMapping("/now")
    public ResponseEntity<?> getWeather() {

        List<WeatherItemDTO> cacheData = cacheManageService.get("weather", "", List.class);

        if (cacheData != null) return ResponseEntity.ok().body(cacheData);

        List<WeatherItemDTO> data = weatherService.getWeatherForecast();
        if (data == null) return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok().body(data);
    }

    @RequestMapping("/cache/delete")
    public ResponseEntity<?> deleteCache() {
        cacheManageService.forget("weather", "");
        return ResponseEntity.ok().body(Map.of("msg", "캐시를 삭제하였습니다."));
    }
}