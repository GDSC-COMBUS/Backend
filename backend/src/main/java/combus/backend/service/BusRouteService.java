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

    @Autowired
    private RestTemplate restTemplate;

    private final BusMatchRepository busMatchRepository;  // BusMatchRepository 추가


    @Value("${serviceKey}")
    String serviceKey;

    // 버스 노선 번호로 해당 버스가 경유하는 정류장 리스트를 넘겨주는 공공 데이터 url
    String getRouteInfoURL = "http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?";

    public BusRouteService(RestTemplate restTemplate, BusMatchRepository busMatchRepository) {
        this.restTemplate = restTemplate;
        this.busMatchRepository = busMatchRepository;
    }

    //    public List<BusStopDto> getBusStopsByDriverId(Long driverId) throws Exception {
//        Optional<BusMatch> busMatchOptional = BusMatchRepository.findBusRouteIdByDriverId(driverId);
//
//        if (busMatchOptional.isPresent()) {
//            BusMatch busMatch = busMatchOptional.get();
//            Long busRouteId = busMatch.getBusId();
//
//            // Use RestTemplate to make a request to the public bus route information API
//            String url = getRouteInfoURL + "ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
//            System.out.println(url);
//
//            URI uri = new URI(url);
//            String xmlData = restTemplate.getForObject(uri, String.class);
//
//            // Parse the XML response and retrieve the list of bus stops
//            return parseXmlWithDom(xmlData);
//        } else {
//            // Handle the case when no BusMatch is found for the given driverId
//            throw new ChangeSetPersister.NotFoundException("BusMatch not found for driverId: " + driverId);
//        }
//    }
public List<BusStopDto> getBusRoutesByDriverId(Long driverId) {
    try {
        // BusMatchRepository를 통해 노선 조회
        Optional<BusMatch> busMatchOptional = busMatchRepository.findBusRouteIdByDriverId(driverId);

        if (busMatchOptional.isPresent()) {
            BusMatch busMatch = busMatchOptional.get();
            Long busRouteId = busMatch.getBusId();

            // 공공 버스 노선 정보 API에 요청을 보내고 응답을 파싱하여 정류장 리스트를 반환
            String url = getRouteInfoURL + "ServiceKey=" + serviceKey + "&busRouteId=" + busRouteId;
            URI uri = new URI(url);
            String xmlData = restTemplate.getForObject(uri, String.class);
            return parseXmlWithDom(xmlData);
        } else {
            // BusMatch가 없는 경우에 대한 처리
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BusMatch not found for driverId: " + driverId);
        }
    } catch (Exception e) {
        // 예외 처리
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", e);
    }
}

    public List<BusStopDto> parseXmlWithDom(String xml) throws Exception {
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

                BusStopDto busStopDto;

                String arsId = getElementValue(itemListElement, "arsId");
                String name = getElementValue(itemListElement, "stationNm");

                busStopDto = new BusStopDto(arsId, name);
                busRoutes.add(busStopDto);
            }
        }
        return busRoutes;
    }

    private String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}
