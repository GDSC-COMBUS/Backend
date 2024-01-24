package combus.backend.controller;

import combus.backend.domain.BusMatch;
import combus.backend.dto.BusResponseDto;
import combus.backend.dto.BusStopDto;
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

    String getRouteInfoURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";



    @GetMapping("/home")
    public ResponseEntity<List<BusStopDto>> getBusRoutesByDriverId(
            @RequestParam("driverId") Long driverId
    ) throws Exception {
        // Your code to retrieve busRouteId from the database using driverId
        Optional<BusMatch> busMatchOptional = busMatchRepository.findBusRouteIdByDriverId(driverId);

        if (busMatchOptional.isPresent()) {
            BusMatch busMatch = busMatchOptional.get();
            Long busRouteId = busMatch.getBusId();

            // Use RestTemplate to make a request to the public bus route information API
            String url = getRouteInfoURL + "ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
            System.out.println(url);

            URI uri = new URI(url);
            String xmlData = restTemplate.getForObject(uri, String.class);

            // Parse the XML response and retrieve the list of bus stops
            List<BusStopDto> busStopList = busRouteService.parseXmlWithDom(xmlData);

            return new ResponseEntity<>(busStopList, HttpStatus.OK);
        } else {
            // Handle the case when no BusMatch is found for the given driverId
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
