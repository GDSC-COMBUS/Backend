//driverController
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

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }


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

        Long driverId = driver.getId();

        return ResponseData.toResponseEntity(ResponseCode.SIGNIN_SUCCESS, loginDriver);
    }
}
