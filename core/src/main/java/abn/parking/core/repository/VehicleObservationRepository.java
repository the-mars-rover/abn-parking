package abn.parking.core.repository;

import abn.parking.core.entity.VehicleObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleObservationRepository extends JpaRepository<VehicleObservation, Long> {
    List<VehicleObservation> findAllByVerifiedIsFalse();
}
