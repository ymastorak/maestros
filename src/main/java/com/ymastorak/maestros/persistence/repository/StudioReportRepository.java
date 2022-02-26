package com.ymastorak.maestros.persistence.repository;

import com.ymastorak.maestros.persistence.model.StudioReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudioReportRepository extends JpaRepository<StudioReport, Long> {
    List<StudioReport> findReportsByMemberId(int memberId);

    @Query("select s from StudioReport s where s.memberId = ?1 and s.studioUsageHours = ?2 and s.periodStartDate = ?3 and s.periodEndDate = ?4")
    Optional<StudioReport> findReport(Integer memberId, int studioUsageHours, LocalDate periodStartDate, LocalDate periodEndDate);

    @Query("select s from StudioReport s where s.memberId = ?1 and ((s.periodStartDate <= ?2 and s.periodEndDate >= ?2) or (s.periodStartDate <= ?3 and s.periodEndDate >= ?3))")
    List<StudioReport> findOverlappingReports(int memberId, LocalDate periodStart, LocalDate periodEnd);
}
