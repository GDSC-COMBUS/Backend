package combus.backend.controller;

import combus.backend.domain.Bus;
import combus.backend.domain.BusMatch;
import combus.backend.dto.BusResponseDto;
import combus.backend.dto.BusStopDto;
import combus.backend.dto.DriverHomeBusStopDto;
import combus.backend.dto.DriverHomeResponseDto;
import combus.backend.repository.BusMatchRepository;
import combus.backend.service.BusRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class BusRouteController {

    private final BusRouteService busRouteService;
    private final BusMatchRepository busMatchRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${serviceKey}")
    String serviceKey;

    // 버스 노선 ID 사용해서 정류장 정보 가져오기
    String getRouteInfoURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";



    @GetMapping("/home")
    public ResponseEntity<List<DriverHomeResponseDto>> getBusRoutesByDriverId(
            @SessionAttribute(name = "userId", required = false)Long driverId
    ) throws Exception {

        //현재 로그인한 버스기사가 운전하는 버스의 vehID 가져오기
        Optional<BusMatch> busMatchOptional = busMatchRepository.findBusMatchByDriverId(driverId);

        if (busMatchOptional.isPresent()) {
            BusMatch busMatch = busMatchOptional.get();

            String busRouteId = busMatch.getBus().getBusRouteId();
            String busRouteName = busMatch.getBus().getBusRouteName();
            Long vehId = busMatch.getBus().getVehId();

            // busRouteID를 사용해서 해당 버스의 노선 리턴
            String url = getRouteInfoURL + "ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
            System.out.println(url);

            URI uri = new URI(url);
            String xmlData = restTemplate.getForObject(uri, String.class);

            // Parse the XML response and retrieve the list of bus stops
            List<DriverHomeBusStopDto> busStopList = busRouteService.getDriverRouteInfo(xmlData, vehId);


            return new ResponseEntity<>(busStopList, HttpStatus.OK);
        } else {
            // Handle the case when no BusMatch is found for the given driverId
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
