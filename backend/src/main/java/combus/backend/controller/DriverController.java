package combus.backend.controller;

import combus.backend.domain.Driver;
import combus.backend.dto.BusResponseDto;
import combus.backend.dto.BusStopDto;
import combus.backend.dto.LoginDriverResponseDto;
import combus.backend.repository.DriverRepository;
import combus.backend.request.LoginRequest;
import combus.backend.service.BusRouteService;
import combus.backend.service.DriverService;
import combus.backend.util.ResponseCode;
import combus.backend.util.ResponseData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;
    private final DriverRepository driverRepository;
    private final BusRouteService busRouteService;

    @Autowired
    public DriverController(DriverService driverService, DriverRepository driverRepository, BusRouteService busRouteService) {
        this.driverService = driverService;
        this.driverRepository = driverRepository;
        this.busRouteService = busRouteService;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Value("${serviceKey}")
    String serviceKey;



    @PostMapping("/login")
    public ResponseEntity<ResponseData<LoginDriverResponseDto>> login(@RequestBody LoginRequest LoginRequest, BindingResult bindingResult,
                                                                      HttpServletRequest httpServletRequest) {

        String loginId = LoginRequest.getLoginId();
        System.out.println("loginId: " + loginId);

        Driver driver = driverService.authenticateDriver(loginId);

        // 회원 번호가 틀린 경우
        if (driver == null) {
            bindingResult.reject("login failed", "로그인 실패! 회원 번호를 확인해주세요.");
        }
        if (bindingResult.hasErrors()) {
            return ResponseData.toResponseEntity(ResponseCode.ACCOUNT_NOT_FOUND, null);
        }

        LoginDriverResponseDto loginDriver = new LoginDriverResponseDto(driver);

        // 로그인 성공 => 세션 생성
        // 세션을 생성하기 전에 기존의 세션 파기
        httpServletRequest.getSession().invalidate();
        HttpSession session = httpServletRequest.getSession(true);  // Session이 없으면 생성

        // 세션에 user의 기본키 Id를 넣어줌 123
        session.setAttribute("userId", driver.getId());
        session.setMaxInactiveInterval(7200); // Session이 2시간동안 유지

        return ResponseData.toResponseEntity(ResponseCode.SIGNIN_SUCCESS, loginDriver);
    }

//    @GetMapping("/home")
//    public List<BusResponseDto> getBusRoutesByDriverId(Long driverId)
//    {
//        try {
//            // BusRouteService를 통해 노선 조회
//           BusResponseDto busRoutes = new BusResponseDto(String vehId, String busRouteId, String busRouteAbrv, int low);
//           return (List<BusResponseDto>) ResponseData.toResponseEntity(ResponseCode.ROUTE_SUCCESS,busRoutes);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}

//    @GetMapping("/home")
//    public ResponseEntity<ResponseData<List<BusStopDto>>> getBusRoutesByDriverId(@RequestParam(value = "driverId") Long driverId) {
//        try {
//            // BusRouteService를 통해 노선 조회
//            List<BusResponseDto> busRoutes = busRouteService.getBusRoutesByDriverId(driverId);
//            return ResponseData.toResponseEntity(ResponseCode.ROUTE_SUCCESS, busRoutes);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    @GetMapping("/home")
//    public ResponseEntity<ResponseData<List<BusStopDto>>> getBusRoutesByDriverId(
//            @RequestParam(value = "driverId") Long driverId
//    ) {
//        try {
//            // BusRouteService를 통해 노선 조회
//            List<BusStopDto> busStopList = busRouteService.getBusRoutesByDriverId(driverId);
//            return ResponseData.toResponseEntity(ResponseCode.ROUTE_SUCCESS, busStopList);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseData.toResponseEntity(ResponseCode.INTERNAL_SERVER_ERROR, null);
//        }
//    }

    // 버스 정류장 ID로 해당 정류장을 경유하는 버스 데이터를 넘겨주는 공공 데이터 url
    String getStationByUidItemURL = "http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?";

    // 버스 노선 번호로 해당 버스가 경유하는 정류장 리스트를 넘겨주는 공공 데이터 url
    String getBusRouteInfoURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";


    @GetMapping("/home")
    public ResponseEntity<List<BusStopDto>> getBusRoutesByDriverId(
            @RequestParam(value = "driverId") Long driverId
    ) throws Exception {
        HttpURLConnection urlConnection = null;
        InputStream stream = null;
        String result = null;
        RestTemplate restTemplate = new RestTemplate();

        // 공공데이터 API 요청을 보낼 url 생성
        String urlStr = getStationByUidItemURL + "ServiceKey=" + serviceKey + "&driverId=" + driverId;
        System.out.println(urlStr);

        URI uri = new URI(urlStr);
        String xmlData = restTemplate.getForObject(uri, String.class);

        List<BusStopDto> busStopList = busRouteService.parseXmlWithDom(xmlData);
        return new ResponseEntity<>(busStopList, HttpStatus.OK);
    }

}