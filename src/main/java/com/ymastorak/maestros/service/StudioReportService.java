package com.ymastorak.maestros.service;

import com.ymastorak.maestros.api.dtos.response.UploadReportResponse;
import com.ymastorak.maestros.persistence.DAO.LockDAO;
import com.ymastorak.maestros.persistence.model.Member;
import com.ymastorak.maestros.persistence.model.StudioReport;
import com.ymastorak.maestros.persistence.repository.StudioReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudioReportService {

    @Qualifier("dateFormatter")
    private final DateTimeFormatter dateTimeFormatter;
    private final MemberService memberService;
    private final StudioReportRepository studioReportRepository;
    private final StudioReportFileParser studioReportFileParser;
    private final LockDAO lockDAO;

    public UploadReportResponse handleReportUpload(MultipartFile reportFile, String startDateStr, String endDateStr) throws IOException {
        lockDAO.lockReports();

        ZonedDateTime uploadDate = ZonedDateTime.now();
        LocalDate startDate = LocalDate.parse(startDateStr, dateTimeFormatter);
        LocalDate endDate = LocalDate.parse(endDateStr, dateTimeFormatter);

        List<Integer> activeMemberIds = memberService.getIdsOfActiveMembers();
        lockActiveMembers(activeMemberIds);
        List<Member> activeMembers = activeMemberIds.stream().map(memberService::getMember).collect(Collectors.toList());

        List<StudioReport> memberReports = studioReportFileParser.parseStudioReportFile(reportFile, startDate, endDate, uploadDate, activeMembers);

        BigDecimal legacyOutstanding = BigDecimal.ZERO;
        BigDecimal currentOutstanding = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        List<Member> reponseMembers = new ArrayList<>(activeMemberIds.size());
        for (Member member : activeMembers) {

            StudioReport memberReport = findMemberReport(member.getId(), memberReports);
            if (memberReport == null) {
                memberReport = createEmptyMemberReport(member, startDate, endDate, uploadDate);
            }
            memberReport = persist(memberReport, member); // persist here so report id is set from database

            member = memberService.applyStudioReportCharges(member, memberReport);
            reponseMembers.add(member);

            legacyOutstanding = legacyOutstanding.add(member.getLegacyOutstanding());
            currentOutstanding = currentOutstanding.add(member.getCurrentOutstanding());
            totalOutstanding = totalOutstanding.add(member.getTotalOutstanding());
        }

        return UploadReportResponse.builder()
                .members(reponseMembers)
                .legacyOutstanding(legacyOutstanding)
                .currentOutstanding(currentOutstanding)
                .totalOutstanding(totalOutstanding)
                .build();
    }

    public List<StudioReport> getMemberReports(int memberId) {
        return studioReportRepository.findReportsByMemberId(memberId);
    }

    private StudioReport findMemberReport(int memberId, List<StudioReport> reports) {
        for (StudioReport report : reports) {
            if (report.getMemberId() == memberId) {
                return report;
            }
        }
        return null;
    }

    private StudioReport createEmptyMemberReport(Member member, LocalDate startDate, LocalDate endDate, ZonedDateTime uploadDate) {
        return StudioReport.builder()
                .memberId(member.getId())
                .studioUsageHours(0)
                .periodStartDate(startDate)
                .periodEndDate(endDate)
                .uploadDate(uploadDate)
                .build();
    }

    private StudioReport persist(StudioReport report, Member member) {
        Optional<StudioReport> existingReportOpt = studioReportRepository
                .findReport(report.getMemberId(), report.getStudioUsageHours(), report.getPeriodStartDate(), report.getPeriodEndDate());
        if (existingReportOpt.isPresent()) {
            throw new MaestrosLogicException("Studio report for "+member+": "+report+" overlaps with existing report "+existingReportOpt.get());
        }
        return studioReportRepository.save(report);
    }

    private void lockActiveMembers(List<Integer> activeMemberIds) {
        Collections.sort(activeMemberIds);
        for (int memberId : activeMemberIds) {
            lockDAO.lockMember(memberId);
        }
    }
}