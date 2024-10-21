package abn.parking.core.repository;

import abn.parking.core.entity.ParkingSession;
import abn.parking.core.entity.VehicleObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    Optional<ParkingSession> findByLicenseAndEndInstantIsNull(String license);

    @Query("""
        SELECT s FROM abn.parking.core.entity.ParkingSession s
        WHERE s.license = :#{#observation.license}
        AND s.street = :#{#observation.street}
        AND s.startInstant < :#{#observation.observationInstant}
        AND (s.endInstant >= :#{#observation.observationInstant} OR s.endInstant IS NULL)""")
    Optional<ParkingSession> findSessionForObservation(@Param("observation") VehicleObservation observation);
}
