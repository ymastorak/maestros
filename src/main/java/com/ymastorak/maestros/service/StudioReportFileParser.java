package com.ymastorak.maestros.service;

import com.ymastorak.maestros.persistence.model.StudioReport;
import com.ymastorak.maestros.utils.Utils;
import com.ymastorak.maestros.persistence.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudioReportFileParser {

    public List<StudioReport> parseStudioReportFile(MultipartFile reportFile,
                                                    LocalDate startDate,
                                                    LocalDate endDate,
                                                    ZonedDateTime uploadDate,
                                                    List<Member> activeMembers) throws IOException {
        List<StudioReport> memberReports = new ArrayList<>();
        List<String> lines = new BufferedReader(new InputStreamReader(reportFile.getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        for (int i = 0; i < lines.size(); i++) {
            Optional<StudioReport> memberReportOpt = handleReportLine(i + 1, lines.get(i), startDate, endDate, uploadDate, activeMembers);
            memberReportOpt.ifPresent(memberReport -> memberReports.add(memberReport));
        }
        return memberReports;
    }

    private Optional<StudioReport> handleReportLine(int lineNumber, String line, LocalDate startDate,
                                                    LocalDate endDate, ZonedDateTime uploadDate, List<Member> activeMembers) {
        String[] entries = line.split(",");

        if (entries.length != 2 || entries[0].toLowerCase(Locale.ROOT).contains("user")) {
            return Optional.empty();
        }

        String normalizedName = Utils.normalizeGreekString(entries[0]);
        int hours = Utils.extractInteger(entries[1]);

        Optional<Member> memberOpt = findMemberByReportName(activeMembers, normalizedName);
        if (memberOpt.isPresent()) {
            return Optional.of(StudioReport.builder()
                    .memberId(memberOpt.get().getId())
                    .periodStartDate(startDate)
                    .studioUsageHours(hours)
                    .periodEndDate(endDate)
                    .uploadDate(uploadDate)
                    .build());
        } else {
            throw new MaestrosLogicException("Cannot find active member to match report line " + lineNumber + ": <" + line + ">");
        }
    }

    private Optional<Member> findMemberByReportName(List<Member> activeMembers, String reportName) {
        for (Member member : activeMembers) {
            if (reportNameMatchesMember(reportName, member)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    private boolean reportNameMatchesMember(String reportName, Member member) {
        String memberName = Utils.normalizeGreekString(member.getName() + member.getSurname());
        if (memberName.equals(reportName)) {
            return true;
        }
        if (member.getReportsName() != null) {
            String memberReportName = Utils.normalizeGreekString(member.getReportsName());
            if (memberReportName.equals(reportName)) {
                return true;
            }
        }
        return false;
    }
}
