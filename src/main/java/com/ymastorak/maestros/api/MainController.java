package com.ymastorak.maestros.api;

import com.ymastorak.maestros.api.dtos.request.*;
import com.ymastorak.maestros.api.dtos.response.MaestrosServiceResponse;
import com.ymastorak.maestros.api.dtos.response.MemberPaymentResponse;
import com.ymastorak.maestros.api.dtos.response.MemberRelatedResponse;
import com.ymastorak.maestros.api.dtos.response.UploadReportResponse;
import com.ymastorak.maestros.persistence.model.ConfigurationProperty;
import com.ymastorak.maestros.persistence.model.Event;
import com.ymastorak.maestros.persistence.model.Member;
import com.ymastorak.maestros.persistence.model.StudioReport;
import com.ymastorak.maestros.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/maestros/api/v1")
@RequiredArgsConstructor
@Transactional(Transactional.TxType.REQUIRED)
public class MainController {

    private final MemberService memberService;
    private final StudioReportService studioReportService;
    private final EventService eventService;
    private final ConfigurationService configurationService;
    private final ExceptionsHandler exceptionsHandler;

    // Studio
    @PostMapping(value = "/uploadStudioReport", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UploadReportResponse uploadStudioReport(@RequestParam("startDate") String startDate,
                                                   @RequestParam("endDate") String endDate,
                                                   @RequestParam("file") MultipartFile reportFile) throws IOException {
        return studioReportService.handleReportUpload(reportFile, startDate, endDate);
    }

    // Members
    @PostMapping(value = "/registerMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse registerMember(@Valid @RequestBody MemberRegistrationRequest request) {
        return memberService.registerMember(request);
    }

    @PostMapping(value = "/updateMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse updateMember(@Valid @RequestBody MemberUpdateRequest request) {
        return memberService.updateMember(request);
    }

    @PostMapping(value = "/deactivateMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse deactivateMember(@Valid @RequestBody MemberRelatedRequest request) {
        return memberService.deactivateMember(request);
    }

    @PostMapping(value = "/reactivateMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse reactivateMember(@Valid @RequestBody MemberRelatedRequest request) {
        return memberService.reactivateMember(request);
    }

    @PostMapping(value = "/blacklistMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse blacklistMember(@Valid @RequestBody MemberBlacklistRequest request) {
        return memberService.blacklistMember(request);
    }

    @PostMapping(value = "/assignCardToMember", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse assignCardToMember(@Valid @RequestBody MemberCardAssignRequest request) {
        return memberService.assignCard(request);
    }

    @PostMapping(value = "/applyMemberPayment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberPaymentResponse applyMemberPayment(@Valid @RequestBody MemberPaymentRequest request) {
        return memberService.applyMemberRepayment(request);
    }

    @PostMapping(value = "/updateMemberOutstanding", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse updateMemberOutstanding(@Valid @RequestBody MemberOutstandingUpdateRequest request) {
        return memberService.updateMemberOutstanding(request);
    }

    @PostMapping(value = "/updateMemberPresence", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse updateMemberPresence(@Valid @RequestBody MemberRelatedRequest request) {
        return memberService.updateMemberPresence(request);
    }

    @PostMapping(value = "/addMemberAbsence", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse addMemberAbsence(@Valid @RequestBody MemberRelatedRequest request) {
        return memberService.addAbsence(request);
    }

    @PostMapping(value = "/addMemberJustifiedAbsence", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MemberRelatedResponse addMemberJustifiedAbsence(@Valid @RequestBody MemberRelatedRequest request) {
        return memberService.addJustifiedAbsence(request);
    }

    @GetMapping(value = "/getMembers", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> getMembers() {
        return memberService.getMembers();
    }

    @GetMapping(value = "/getMembersToDeactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> getMembersToDeactivate() {
        return memberService.getMembersToDeactivate();
    }

    @GetMapping(value = "/getMembersNotPresentAfter", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Member> getMembersNotPresentAfter(String date) {
        return memberService.getMembersNotPresentAfter(date);
    }

    @GetMapping(value = "/getMemberReports", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StudioReport> getMemberReports(@RequestParam @Min(1) Integer memberId) {
        return studioReportService.getMemberReports(memberId);
    }

    @GetMapping(value = "/getMemberEvents", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Event> getMemberEvents(@RequestParam @Min(1) Integer memberId) {
        return eventService.getEventsByMemberId(memberId);
    }

    // Configuration
    @PostMapping(value = "/updateConfiguration", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConfigurationProperty> updateConfiguration(@Valid @RequestBody Map<String, String> request) {
        return configurationService.updateConfiguration(request);
    }

    @GetMapping(value = "/getConfiguration", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConfigurationProperty> getConfiguration() {
        return configurationService.getConfiguration();
    }

    @ExceptionHandler
    public ResponseEntity<MaestrosServiceResponse> handleExceptions(Exception ex) {
        return exceptionsHandler.handleException(ex);
    }
}
