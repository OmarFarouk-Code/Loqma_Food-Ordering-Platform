package com.loqma.auth.repository;

import com.loqma.driver.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverLookupRepository extends JpaRepository<Driver, String> {
    boolean existsByNationalId(String nationalId);
    boolean existsByPlateNumber(String plateNumber);
    boolean existsByDrivingLicense(String drivingLicense);
}
