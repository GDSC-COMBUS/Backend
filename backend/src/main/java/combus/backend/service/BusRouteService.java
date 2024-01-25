//BusRouteService
package combus.backend.service;

import combus.backend.domain.BusMatch;
import combus.backend.dto.BusStopDto;
import combus.backend.repository.BusMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BusRouteService {

    private RestTemplate restTemplate;
    private BusMatchRepository busMatchRepository;

    @Value("${serviceKey}")
    private String serviceKey;

    String getRouteInfoURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";

    public List<BusStopDto> getBusRoutesByDriverId(Long driverId) {
        try {
            // BusMatchRepository를 통해 노선 조회
            Optional<BusMatch> busMatchOptional = busMatchRepository.findBusRouteIdByDriverId(driverId);

            if (busMatchOptional.isPresent()) {
                BusMatch busMatch = busMatchOptional.get();
                Long busId = busMatch.getBusId();

                // 1. 첫 번째 API 호출: http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList
                String getBusRouteListURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList";
                String routeListUrl = getBusRouteListURL + "?ServiceKey=" + serviceKey + "&strSrch=" + busId;
                URI routeListUri = new URI(routeListUrl);
                String routeListXmlData = restTemplate.getForObject(routeListUri, String.class);

                // 2. XML 파싱을 통해 busRouteId 추출
                Long busRouteId = parseXmlToGetBusRouteId(routeListXmlData);

                // 3. 두 번째 API 호출: http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute
                String getStaionByRouteURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute";
                String stationUrl = getStaionByRouteURL + "?ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
                URI stationUri = new URI(stationUrl);
                String stationXmlData = restTemplate.getForObject(stationUri, String.class);

                // 4. XML 파싱을 통해 정류장 리스트 추출
                return parseXmlToGetStations(stationXmlData);
            } else {
                // BusMatch가 없는 경우에 대한 처리
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BusMatch not found for driverId: " + driverId);
            }
        } catch (Exception e) {
            // 예외 처리
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", e);
        }
    }

    public List<BusStopDto> parseXmlToGetStations(String xml) throws Exception {
        List<BusStopDto> busRoutes = new ArrayList<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            NodeList nodeList = document.getElementsByTagName("item");
            System.out.println("itemlist개수: "+ nodeList.getLength());


        for (int i = 0; i < nodeList.getLength(); i++) {
            Node itemListNode = nodeList.item(i);

            if (itemListNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemListElement = (Element) itemListNode;


                String arsId = getElementValue(itemListElement, "arsId");
                String name = getElementValue(itemListElement, "stationNm");

                BusStopDto busStopDto = new BusStopDto(arsId, name);
                busRoutes.add(busStopDto);
            }
        }
        return busRoutes;
    }

    private Long parseXmlToGetBusRouteId(String xml) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        NodeList nodeList = document.getElementsByTagName("item");

        if (nodeList.getLength() > 0) {
            Node itemNode = nodeList.item(0);
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) itemNode;

                // XML에서 busRouteId 추출
                String busRouteIdString = getElementValue(itemElement, "busRouteId");
                return Long.parseLong(busRouteIdString);
            }
        }

        // 파싱에 실패한 경우
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse busRouteId from XML");
    }

    private String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}
