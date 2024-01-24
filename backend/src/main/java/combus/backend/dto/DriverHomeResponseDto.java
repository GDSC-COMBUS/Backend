package combus.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class DriverHomeResponseDto {
    private String vehId;           // 버스 고유 ID
    private String busRouteName;    // 버스 노선 번호
    private int totalReserved;      // 총 승차 예약 인원수
    private int totalOnboarding;    // 총 하차 예정 인원수

    private double gpsX;            // 버스 현재 위치 X 좌표
    private double gpsY;            // 버스 현재 위치 Y 좌표

    private List<DriverHomeBusStopDto> BusStopList;
}
