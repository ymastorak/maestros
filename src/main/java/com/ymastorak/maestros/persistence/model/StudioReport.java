package com.ymastorak.maestros.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name = "studio_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudioReport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(nullable = false)
    private Integer memberId;
    @Column(nullable = false)
    private Integer studioUsageHours;
    @Column(nullable = false)
    private LocalDate periodStartDate; // included in period
    @Column(nullable = false)
    private LocalDate periodEndDate;   // included in period
    @Column(nullable = false)
    private ZonedDateTime uploadDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudioReport report = (StudioReport) o;
        return Objects.equals(studioUsageHours, report.studioUsageHours)
                && memberId.equals(report.memberId)
                && periodStartDate.equals(report.periodStartDate)
                && periodEndDate.equals(report.periodEndDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, periodStartDate, periodEndDate);
    }
}
