package yh_project.openapi.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import yh_project.openapi.cache.CacheManageService;
import yh_project.openapi.util.TimeUtil;
import yh_project.openapi.weather.dto.FcstResDTO;
import yh_project.openapi.weather.dto.TimeParamDTO;
import yh_project.openapi.weather.dto.WeatherItemDTO;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final int MAX_RETRY = 5; // 재시도 횟수 조정
    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    @Value("${env.api_key}")
    private String apikey;

    public final CacheManageService cacheManageService;
    private final RestClient restClient = RestClient.builder().build();

    /**
     * 초단기예보를 활용한 지역별 간편 날씨 조회
     */
    public List<WeatherItemDTO> getWeatherForecast() {
        List<WeatherItemDTO> weatherList = new ArrayList<>();

        // 1. 시간 파라미터 생성 (TimeUtil 구현 가정)
        // 초단기예보는 매시 30분에 생성되므로, 45분 이후 호출 시 해당 시간 데이터를 가져옵니다.
        TimeParamDTO baseDateTimeParam = TimeUtil.getDateTimeParamOfUltraSrtFcst();

        // 현재와 가장 가까운 미래의 예보 시간을 타겟으로 함
        String targetFcstTime = TimeUtil.getForecastDateTimeNow();

        log.info("API Request Info: Date={}, Time={}, TargetFcst={}",
                baseDateTimeParam.getBaseDate(), baseDateTimeParam.getBaseTime(), targetFcstTime);

        // 2. 지역별 순회 (병렬 처리를 권장하지만, 기본 요구사항에 맞춰 순차 처리)
        for (Region region : Region.values()) {
            try {
                // 변수 임시 저장소
                String tempT1H = null; // 기온
                String codeSKY = null; // 하늘상태
                String codePTY = null; // 강수형태
                String codeLGT = null; // 낙뢰

                URI fcstURI = this.getUltraSrtFcstURI(baseDateTimeParam, region);
                ResponseEntity<FcstResDTO> response = getWeatherAPI(fcstURI);

                if (!isValidResponse(response)) {
                    log.warn("Empty response for region: {}", region.getName());
                    continue;
                }

                List<FcstResDTO.Item> items = response.getBody().getResponse().getBody().getItems().getItem();

                // 3. 응답 데이터 파싱
                for (FcstResDTO.Item item : items) {
                    // 가장 가까운 예보 시간의 데이터만 추출
                    if (!item.getFcstTime().equals(targetFcstTime)) continue;

                    switch (item.getCategory()) {
                        case "T1H": // 기온
                            tempT1H = item.getFcstValue();
                            break;
                        case "SKY": // 하늘상태 (1:맑음, 3:구름많음, 4:흐림) [
                            codeSKY = item.getFcstValue();
                            break;
                        case "PTY": // 강수형태 (0:없음, 1:비, 2:비/눈, 3:눈, 5:빗방울, 6:빗방울눈날림, 7:눈날림)
                            codePTY = item.getFcstValue();
                            break;
                        case "LGT": // 낙뢰 (kA)
                            codeLGT = item.getFcstValue();
                            break;
                    }
                }

                // 4. 날씨 상태 우선순위 결정 및 DTO 생성
                WeatherItemDTO dto = new WeatherItemDTO();
                dto.setRegion(region.getName());
                dto.setTime(targetFcstTime);
                dto.setTemperature(tempT1H);
                dto.setStatus(determineWeatherStatus(codeSKY, codePTY, codeLGT));

                weatherList.add(dto); // **기존 코드 누락 수정: 리스트에 추가**

            } catch (Exception e) {
                log.error("Failed to fetch weather for {}: {}", region.getName(), e.getMessage());
            }
        }

        // 캐시 저장 로직 유지
        cacheManageService.put("weather", "", weatherList);
        return weatherList;
    }

    /**
     * 날씨 상태 결정 로직 (우선순위: 번개 > 비/눈 > 흐림/맑음)
     */
    private WeatherStatus determineWeatherStatus(String sky, String pty, String lgt) {
        // 1. 낙뢰 체크 (LGT)
        // API 명세: 초단기예보에서 LGT 단위는 kA(킬로암페어)
        if (lgt != null) {
            try {
                double lgtValue = Double.parseDouble(lgt);
                if (lgtValue > 0) return WeatherStatus.LIGHTNING;
            } catch (NumberFormatException ignored) {}
        }

        // 2. 강수 형태 체크 (PTY)
        // API 명세: 0(없음), 1(비), 2(비/눈), 3(눈), 5(빗방울), 6(빗방울눈날림), 7(눈날림)
        if (pty != null && !pty.equals("0")) {
            switch (pty) {
                case "3":
                case "7":
                    return WeatherStatus.SNOW;
                case "2":
                case "6":
                    return WeatherStatus.RAIN_SNOW;
                case "1":
                case "5":
                default:
                    return WeatherStatus.RAIN;
            }
        }

        // 3. 하늘 상태 체크 (SKY)
        // API 명세: 1(맑음), 3(구름많음), 4(흐림)
        if (sky != null) {
            switch (sky) {
                case "4": return WeatherStatus.OVERCAST;
                case "3": return WeatherStatus.CLOUDY;
                case "1": return WeatherStatus.SUNNY;
            }
        }

        return WeatherStatus.UNKNOWN;
    }

    private URI getUltraSrtFcstURI(TimeParamDTO dateTimeParam, Region region) {
        // API 가이드에 따라 필수 파라미터 조합
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst" +
                "?serviceKey=" + apikey +
                "&pageNo=1" +
                "&numOfRows=60" + // 모든 카테고리를 한번에 받기 위해 넉넉하게 설정
                "&dataType=JSON" +
                "&base_date=" + dateTimeParam.getBaseDate() +
                "&base_time=" + dateTimeParam.getBaseTime() +
                "&nx=" + region.getNx() +
                "&ny=" + region.getNy();
        return URI.create(url);
    }

    // 응답 유효성 검사 헬퍼 메소드
    private boolean isValidResponse(ResponseEntity<FcstResDTO> response) {
        return response != null && response.getBody() != null
                && response.getBody().getResponse() != null
                && response.getBody().getResponse().getBody() != null
                && response.getBody().getResponse().getBody().getItems() != null;
    }

    public ResponseEntity<FcstResDTO> getWeatherAPI(URI uri) {
        // 기존 재시도 로직 유지 (생략 가능하나 안전성을 위해 포함)
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                return restClient.get().uri(uri).retrieve().toEntity(FcstResDTO.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.info("API 429 Retry {}/{}", i + 1, MAX_RETRY);
                try { sleep(RETRY_DELAY.toMillis()); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.error("API Call Error: {}", e.getMessage());
                break;
            }
        }
        return null;
    }
}