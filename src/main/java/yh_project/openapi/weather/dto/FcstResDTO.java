package yh_project.openapi.weather.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class FcstResDTO {
    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode; // 결과 코드 (00: 정상)
        private String resultMsg;  // 결과 메시지 (NORMAL_SERVICE)
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private String dataType;   // 응답자료형식 (XML/JSON)
        private Items items;       // 데이터 목록
        private int pageNo;        // 페이지 번호
        private int numOfRows;     // 한 페이지 결과 수
        private int totalCount;    // 데이터 총 개수
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String baseDate;   // 발표일자 (YYYYMMDD)
        private String baseTime;   // 발표시각 (HHMM)

        private String category;   // 자료구분코드 (예: T1H, RN1, SKY, PTY 등)

        private String fcstDate;   // 예측일자 (YYYYMMDD)
        private String fcstTime;   // 예측시간 (HHMM)

        private String fcstValue;  // 예보 값

        private int nx;            // 예보지점 X 좌표
        private int ny;            // 예보지점 Y 좌표
    }
}
