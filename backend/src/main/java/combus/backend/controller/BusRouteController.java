package combus.backend.controller;

import combus.backend.domain.BusMatch;
import combus.backend.dto.BusStopDto;
import combus.backend.service.BusRouteService;
import combus.backend.repository.BusMatchRepository;
import combus.backend.util.ResponseCode;
import combus.backend.util.ResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class BusRouteController {

    private final BusRouteService busRouteService;
    private final BusMatchRepository busMatchRepository;

    @GetMapping("/drivers/home")
    public ResponseEntity<ResponseData<List<BusStopDto>>> getBusRoutesByDriverId(
            @RequestParam("driverId") Long driverId
    ) {
        try {
            // BusMatchRepository를 통해 노선 조회
            Optional<BusMatch> busMatchOptional = busMatchRepository.findBusRouteIdByDriverId(driverId);

            if (busMatchOptional.isPresent()) {
                BusMatch busMatch = busMatchOptional.get();
                Long busRouteId = busMatch.getBusId();

                // 공공 버스 노선 정보 API에 요청을 보내고 응답을 파싱하여 정류장 리스트를 반환
                List<BusStopDto> busStopList = busRouteService.getBusRoutesByDriverId(driverId);

                return ResponseData.toResponseEntity(ResponseCode.ROUTE_SUCCESS, busStopList);
            } else {
                // BusMatch가 없는 경우에 대한 처리
                return ResponseData.toResponseEntity(ResponseCode.MATCH_NOT_FOUND, null);
            }
        } catch (Exception e) {
            // 예외 처리
            return ResponseData.toResponseEntity(ResponseCode.INTERNAL_SERVER_ERROR, null);
        }
    }
}
