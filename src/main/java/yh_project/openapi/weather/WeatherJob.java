package yh_project.openapi.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yh_project.openapi.weather.dto.WeatherItemDTO;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherJob {
    private final WeatherService weatherService;

    @Scheduled(cron = "0 45 * * * *", zone = "Asia/Seoul")
    public void weatherJob() {
        log.info("_____Weather Job Start_____");
        List<WeatherItemDTO> currentWeather = weatherService.getWeatherForecast();
        log.info("api response data = {}", currentWeather);
        log.info("_____Weather Job End_____");
    }
}
