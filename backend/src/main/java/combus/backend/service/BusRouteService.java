package combus.backend.service;

import combus.backend.domain.BusMatch;
import combus.backend.dto.BusStopDto;
import combus.backend.dto.DriverHomeBusStopDto;
import combus.backend.repository.BusMatchRepository;
import combus.backend.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator;
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
@RequiredArgsConstructor
public class BusRouteService {

    @Autowired
    private final ReservationRepository reservationRepository;


    public List<DriverHomeBusStopDto> getDriverRouteInfo(String xml, Long vehId) throws Exception {
        List<DriverHomeBusStopDto> driverHomeBusStopDtoList = new ArrayList<>();

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

                Long arsId = Long.parseLong(getElementValue(itemListElement, "arsId"));
                String name = getElementValue(itemListElement, "stationNm");
                double gpsX = Double.parseDouble(getElementValue(itemListElement, "gpsX"));
                double gpsY = Double.parseDouble(getElementValue(itemListElement, "gpsY"));
                int seq = Integer.parseInt(getElementValue(itemListElement, "seq"));

                // 해당 정류장 하차 인원 알아내기



                DriverHomeBusStopDto driverHomeBusStopDto = new DriverHomeBusStopDto(arsId, name, gpsX, gpsY, seq);
                driverHomeBusStopDtoList.add(driverHomeBusStopDto);
            }
        }
        return driverHomeBusStopDtoList;
    }

    private String getElementValue(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
}
