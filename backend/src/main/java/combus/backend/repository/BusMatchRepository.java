package combus.backend.repository;

import combus.backend.domain.BusMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusMatchRepository extends JpaRepository<BusMatch, Long> {
    Optional<BusMatch> findBusRouteIdByDriverId(Long driverId);
}
