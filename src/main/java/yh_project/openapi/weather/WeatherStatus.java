package yh_project.openapi.weather;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherStatus {
    // 우선순위가 높은 순서(번개 -> 눈/비 -> 흐림 -> 맑음)로 로직에서 활용됩니다.
    LIGHTNING("번개"),
    SNOW("눈"),
    RAIN_SNOW("비/눈"),
    RAIN("비"),
    OVERCAST("흐림"),
    CLOUDY("구름많음"),
    SUNNY("맑음"),
    UNKNOWN("정보없음");

    private final String description;
}
