package abn.parking.core.repository;

import abn.parking.core.entity.ParkingRate;
import abn.parking.core.entity.ParkingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingRateRepository extends JpaRepository<ParkingRate, Long> {
    Optional<ParkingRate> findByStreet(String street);
}
