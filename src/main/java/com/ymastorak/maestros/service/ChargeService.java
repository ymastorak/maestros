package com.ymastorak.maestros.service;

import com.ymastorak.maestros.persistence.model.ChargeType;
import com.ymastorak.maestros.persistence.model.Member;
import com.ymastorak.maestros.persistence.model.MemberType;
import com.ymastorak.maestros.persistence.model.StudioReport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChargeService {

    @Qualifier("dateFormatter")
    private final DateTimeFormatter dateFormatter;
    private final ConfigurationService configurationService;
    private final EventService eventService;

    public Member applyStudioUsageCharge(Member member, StudioReport studioReport) {
        BigDecimal meetingAttendanceCharge = configurationService.getBigDecimalProperty(ConfigurationService.MEETING_ATTENDANCE_CHARGE_PROPERTY_NAME);
        BigDecimal maxAdminUsageCost = configurationService.getBigDecimalProperty(ConfigurationService.MAX_ADMIN_USAGE_COST_PROPERTY_NAME);
        BigDecimal costPerHour = configurationService.getBigDecimalProperty(ConfigurationService.STUDIO_USAGE_COST_PER_HOUR_PROPERTY_NAME);

        BigDecimal usageCost = costPerHour.multiply(new BigDecimal(studioReport.getStudioUsageHours()));

        if (member.getType() == MemberType.SLACKER) {
            if (meetingAttendanceCharge.compareTo(usageCost) > 0) { // if meetingAttendanceCharge > usageCost
                return applyUsageCharge(member, meetingAttendanceCharge, ChargeType.MEETING_ATTENDANCE, studioReport);
            } else {
                return applyUsageCharge(member, usageCost, ChargeType.STUDIO_USAGE, studioReport);
            }
        } else if (member.getType() == MemberType.SOLDIER) {
            if (maxAdminUsageCost.compareTo(usageCost) >= 0) { // if maxAdminUsageCost >= usageCost
                return applyUsageCharge(member, usageCost, ChargeType.STUDIO_USAGE, studioReport);
            } else {
                return applyUsageCharge(member, maxAdminUsageCost, ChargeType.ADMIN_STUDIO_USAGE_MAX, studioReport);
            }
        } else if (member.getType() == MemberType.HERO) {
            return applyUsageCharge(member, usageCost, ChargeType.STUDIO_USAGE, studioReport);
        } else {
            throw new RuntimeException("Unknown member type: " + member.getType() + ", id: " + member.getId());
        }
    }

    public Member applyRegistrationCharge(Member member) {
        BigDecimal registrationCharge = configurationService.getBigDecimalProperty(ConfigurationService.REGISTRATION_CHARGE_PROPERTY_NAME);
        return applyCharge(member, registrationCharge, ChargeType.REGISTRATION);
    }

    public Member applyReactivationCharge(Member member) {
        BigDecimal reactivationCharge = configurationService.getBigDecimalProperty(ConfigurationService.REACTIVATION_CHARGE_PROPERTY_NAME);
        return applyCharge(member, reactivationCharge, ChargeType.REACTIVATION);
    }

    public Member applyAccessCardCharge(Member member) {
        BigDecimal accessCardCharge = configurationService.getBigDecimalProperty(ConfigurationService.ACCESS_CARD_CHARGE);
        return applyCharge(member, accessCardCharge, ChargeType.ACCESS_CARD);
    }

    private Member applyUsageCharge(Member member, BigDecimal amount, ChargeType type, StudioReport report) {
        Map<String, Object> eventPayload = new HashMap<>();
        eventPayload.put("reportId", report.getId());
        eventPayload.put("studioUsageHours", report.getStudioUsageHours());
        eventPayload.put("periodStart", report.getPeriodStartDate().format(dateFormatter));
        eventPayload.put("periodEnd", report.getPeriodEndDate().format(dateFormatter));
        return applyCharge(member, amount, type, eventPayload);
    }

    private Member applyCharge(Member member, BigDecimal amount, ChargeType chargeType) {
        return applyCharge(member, amount, chargeType, null);
    }

    private Member applyCharge(Member member, BigDecimal amount, ChargeType chargeType, Map<String, Object> eventPayload) {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return member;
        }

        ZonedDateTime eventDate = ZonedDateTime.now();
        member
            .applyCharge(amount)
            .setLastUpdateDate(eventDate);

        eventService.addChargeEvent(member, eventDate, chargeType, amount, eventPayload);

        return member;
    }
}
