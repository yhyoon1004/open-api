package yh_project.openapi.weather;

import java.util.Arrays;

public enum Region {
    SEOUL("서울", 60, 127),
    BUSAN("부산", 98, 76),
    DAEGU("대구", 89, 90),
    INCHEON("인천", 55, 124),
    GWANGJU("광주", 58, 74),
    DAEJEON("대전", 67, 100),
    ULSAN("울산", 102, 84),
    GANGNEUNG("강릉", 92, 131),
    JEJU("제주", 52, 38);

    private final String name;
    private final int nx;
    private final int ny;

    Region(String name, int nx, int ny) {
        this.name = name;
        this.nx = nx;
        this.ny = ny;
    }

    public String getName() {
        return name;
    }

    public int getNx() {
        return nx;
    }

    public int getNy() {
        return ny;
    }

    public static Region fromRegionName(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            throw new IllegalArgumentException("해당 지역명이 없습니다.");
        }
        return Arrays.stream(values())
                .filter(r -> r.name.equals(regionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown region : " + regionName));
    }

}
