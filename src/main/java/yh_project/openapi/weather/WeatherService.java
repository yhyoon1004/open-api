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
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    @Value("${env.api_key}")
    private String apikey;

    public final CacheManageService cacheManageService;

    RestClient restClient = RestClient.builder().build();

    /**
     * 현재 날씨 정보 서비스 로직
     * - 공공API에서 초단기예보를 지역 별로 호출해 하늘, 우천, 온도 정보를 받아옴
     */
    public List<WeatherItemDTO> getWeatherForecast() {

        List<WeatherItemDTO> currentWeatherList = new ArrayList<>();

        TimeParamDTO fcstDTParam = TimeUtil.getDateTimeDTOOfUltraSrtFcst();

        log.info("fcstDTParam = " + fcstDTParam);

        //지역 별 api 요청
        for (Region region : Region.values()) {

            long t0 = System.nanoTime();

            //초단기예보 (하늘 상태 정보) 요청
            URI fcstURI = this.getUltraSrtFcstURI(fcstDTParam, region);

            ResponseEntity<FcstResDTO> response = getWeatherAPI(fcstURI);

            //비정상 응답시
            if (response == null) return null;

            FcstResDTO resBodyOfFcst = response.getBody();

            WeatherItemDTO regionWeather = new WeatherItemDTO();
            regionWeather.setRegion(region.getName());

            //정상적으로 api 응답받았을 경우
            if (resBodyOfFcst != null && resBodyOfFcst.getResponse().getBody() != null) {
                for (FcstResDTO.Item item : resBodyOfFcst.getResponse().getBody().getItems().getItem()) {
                    if (item.getFcstTime().equals(fcstDTParam.getTime())) {
                        //예보시간 초기화
                        regionWeather.setTime(item.getFcstTime());

                        //항목별 초기화
                        switch (item.getCategory()) {
                            case "T1H": // 기온 [1]
                                regionWeather.setTemperature(item.getFcstValue() + "℃");
                                break;
                            case "SKY": // 하늘상태 [2]
                                regionWeather.setSky(item.getFcstValue());
                                break;
                            case "PTY": // 강수형태 [2]
                                regionWeather.setPty(item.getFcstValue());
                                break;
                        }
                    }
                }//end for

                //비 or 눈 상태
                String pty = regionWeather.getPty();
                //비가 안내릴 경우
                if (pty == null || pty.isEmpty() || pty.equals("0")) {
                    //하늘상태(SKY) 코드 : 맑음(1), 구름많음(3), 흐림(4)
                    if (regionWeather.getSky() == null) regionWeather.setSky("0");
                    switch (regionWeather.getSky()) {
                        case "0":
                            regionWeather.setIcon("SUNNY_CLOUD");
                            break;
                        case "1":
                            regionWeather.setIcon("SUNNY");
                            break;
                        case "3":
                            regionWeather.setIcon("MANY_CLOUD");
                            break;
                        case "4":
                            regionWeather.setIcon("CLOUD");
                            break;
                    }
                } else {
                    //강수형태(PTY) 코드 : 없음(O), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7)
                    switch (pty) {
                        case "1":
                        case "5":
                            regionWeather.setIcon("RAIN");
                            break;
                        case "2":
                        case "6":
                            regionWeather.setIcon("SLEET");
                            break;
                        case "3":
                        case "7":
                            regionWeather.setIcon("SNOW");
                            break;
                    }
                }//end else
            }//end out if
            currentWeatherList.add(regionWeather);

            long t1 = System.nanoTime();
            log.info(region.getName() + " api response time = " + (t1 - t0) / 1000000 + "ms");
        }

        cacheManageService.put("weather", "", currentWeatherList);

        return currentWeatherList;
    }


    /**
     * 시간,지역 파라미터 제공 필수
     **/
    public URI getUltraSrtFcstURI(TimeParamDTO dateTimeParam, Region region) {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst" +
                "?serviceKey=" + apikey +
                "&pageNo=1" +
                "&numOfRows=60" +
                "&dataType=JSON" +
                "&base_date=" + dateTimeParam.getDate() +
                "&base_time=" + dateTimeParam.getTime() +        // 0600 대신 단기예보 발표 시각인 0500 사용 권장
                "&nx=" + region.getNx() +
                "&ny=" + region.getNy();
        return URI.create(url);
    }

    public ResponseEntity<FcstResDTO> getWeatherAPI(URI uri) {
        ResponseEntity<FcstResDTO> response = null;
        for (int i = 0; i < 10; i++) {
            try {
                response = restClient
                        .get()
                        .uri(uri)
                        .retrieve()
                        .toEntity(FcstResDTO.class);
                if (response.getStatusCode().is2xxSuccessful()) return response;
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.info("api 429 error (try :{})  :  {}", i + 1, e.getMessage());
                response = null;
                try {
                    log.info("wait 1sec");
                    sleep(1000);
                } catch (InterruptedException e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
        return response;
    }

}
