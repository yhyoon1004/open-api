package yh_project.openapi.util;

import yh_project.openapi.weather.dto.TimeParamDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {


    /**
     * 매 시 30분에 날씨정보 생성  -> 45분에 api 제공됨
     * 현재시간이 45분 이후면 시간은 현재 시간값, 이전이면 1시간 전 시간값으로 처리
     * */
    public static TimeParamDTO getDateTimeParamOfUltraSrtFcst() {
        LocalDateTime now = LocalDateTime.now();
        String baseDate = now.format(DateTimeFormatter.BASIC_ISO_DATE);

        int hour = (now.getMinute() >= 45) ? now.getHour() : now.minusHours(1).getHour();
        String baseTime = String.format("%02d%02d", hour, 30);

        return TimeParamDTO.builder().baseDate(baseDate).baseTime(baseTime).build();
    }

    public static String getForecastDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.plusHours(1).getHour();
        return String.format("%02d%02d", hour, 00);
    }
}
