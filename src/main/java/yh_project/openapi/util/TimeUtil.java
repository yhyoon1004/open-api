package yh_project.openapi.util;

import yh_project.openapi.weather.dto.TimeParamDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    public static TimeParamDTO getDateTimeDTOOfUltraSrtFcst() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(30);
        String dateField = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String timeField = String.format("%02d%02d", now.getHour(), now.getMinute() >= 30 ? 30 : 0);
        return TimeParamDTO.builder().date(dateField).time(timeField).build();
    }

    public static final String getFcstHour() {
        LocalDateTime now = LocalDateTime.now().plusHours(1L);
        return String.format("%02d%02d", now.getHour(), 0);
    }

}
