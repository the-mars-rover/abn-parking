package abn.parking.core.repository;

import abn.parking.core.entity.ParkingInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParkingInvoiceRepository extends JpaRepository<ParkingInvoice, Long> {
    List<ParkingInvoice> findAllBySessionLicenseOrObservationLicense(String sessionLicense, String observationLicense);
}
