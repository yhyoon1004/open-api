package yh_project.openapi.util;

import yh_project.openapi.weather.dto.TimeParamDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public static TimeParamDTO getDateTimeDTOOfUltraSrtFcst() {
        LocalDateTime now = LocalDateTime.now();
        String dateField = now.format(DateTimeFormatter.BASIC_ISO_DATE);

        //현재시간이 45분 이후면 시간은 현재 시간값, 이전이면 1시간 전 시간값으로 처리
        int hour = (now.getMinute() >= 45) ? now.getHour() : now.minusHours(1).getHour();
        String hhmm = String.format("%02d%02d", hour, 30);
        return TimeParamDTO.builder().date(dateField).time(hhmm).build();
    }
}
