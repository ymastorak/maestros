package com.ymastorak.maestros.service;

import com.ymastorak.maestros.api.dtos.request.*;
import com.ymastorak.maestros.api.dtos.response.MemberPaymentResponse;
import com.ymastorak.maestros.api.dtos.response.MemberRelatedResponse;
import com.ymastorak.maestros.persistence.DAO.LockDAO;
import com.ymastorak.maestros.persistence.model.*;
import com.ymastorak.maestros.persistence.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChargeService chargeService;
    private final EventService eventService;
    private final LockDAO lockDAO;

    @Qualifier("dateFormatter")
    private final DateTimeFormatter dateFormatter;

    public MemberRelatedResponse registerMember(MemberRegistrationRequest request) {
        ZonedDateTime eventDate = ZonedDateTime.now();

        Member member = Member.builder()
                .name(request.getName())
                .surname(request.getSurname())
                .phone(request.getPhone())
                .email(request.getEmail())
                .type(request.getType())
                .registrationDate(eventDate)
                .legacyOutstanding(BigDecimal.ZERO)
                .currentOutstanding(BigDecimal.ZERO)
                .totalOutstanding(BigDecimal.ZERO)
                .extra(request.getMemberExtra())
                .build();
        member = member.activate(eventDate);
        member = member.updateLastPresenceDate(eventDate);

        member = persist(member, eventDate); // persist member here to create id needed for events

        eventService.addMemberEvent(member, EventType.REGISTRATION, eventDate, request.getEventExtra());

        member = chargeService.applyRegistrationCharge(member);

        if (request.getAccessCardId() != null) {
            member = assignCard(member, request.getAccessCardId(), eventDate, null);
        }

        if (request.getReportsName() != null) {
            member.setReportsName(request.getReportsName());
        }

        member = persist(member, eventDate);
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse updateMember(MemberUpdateRequest request) {
        Member member = getMember(request.getMemberId());
        ZonedDateTime eventDate = ZonedDateTime.now();

        member
                .setName(request.getName())
                .setSurname(request.getSurname())
                .setEmail(request.getEmail())
                .setPhone(request.getPhone())
                .setType(request.getType());

        if (request.getReportsName() != null) {
            member.setReportsName(request.getReportsName());
        }

        if (request.getMemberExtra() != null) {
            member.setExtra(request.getMemberExtra());
        }

        eventService.addMemberEvent(member, EventType.UPDATE, eventDate, request.getEventExtra());

        member = persist(member, eventDate);
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse deactivateMember(MemberRelatedRequest request) {
        Member member = getMember(request.getMemberId());

        if (member.getType() == MemberType.HERO) {
            throw new MaestrosLogicException("Cannot deactivate hero member");
        }

        ZonedDateTime eventDate = ZonedDateTime.now();
        member = member.moveCurrentOutstandingToLegacy();
        member.setStatus(MemberStatus.INACTIVE);

        eventService.addMemberEvent(member, EventType.DEACTIVATION, eventDate, request.getEventExtra());
        member = persist(member, eventDate);

        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse reactivateMember(MemberRelatedRequest request) {
        Member member = getMember(request.getMemberId());

        ZonedDateTime eventDate = ZonedDateTime.now();
        member = member.activate(eventDate);
        member = member.updateLastPresenceDate(eventDate);

        eventService.addMemberEvent(member, EventType.REACTIVATION, member.getRegistrationDate(), request.getEventExtra());

        member = chargeService.applyReactivationCharge(member);
        member = persist(member, eventDate);
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse blacklistMember(MemberBlacklistRequest request) {
        ZonedDateTime eventDate = ZonedDateTime.now();
        Member member = getMember(request.getMemberId());

        member = member.moveCurrentOutstandingToLegacy();
        member
                .setStatus(MemberStatus.BLACKLISTED)
                .setLastUpdateDate(eventDate);

        member = member.updateExtra("blacklistReason", request.getBlacklistReason());

        member = persist(member, eventDate);

        eventService.addBlacklistEvent(member, request.getBlacklistReason(), eventDate, request.getEventExtra());
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse assignCard(MemberCardAssignRequest request) {
        ZonedDateTime eventDate = ZonedDateTime.now();
        Member member = getMember(request.getMemberId());
        if (!request.getAccessCardId().equals(member.getAccessCardId())) {
            member = assignCard(member, request.getAccessCardId(), eventDate, request.getEventExtra());
        }
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse updateMemberOutstanding(MemberOutstandingUpdateRequest request) {
        ZonedDateTime eventDate = ZonedDateTime.now();
        Member member = getMember(request.getMemberId());
        BigDecimal legacyOutstandingBefore = member.getLegacyOutstanding();
        BigDecimal currentOutstandingBefore = member.getCurrentOutstanding();
        member
                .setLegacyOutstanding(request.getLegacyOutstanding())
                .setCurrentOutstanding(request.getCurrentOutstanding())
                .setTotalOutstanding(request.getLegacyOutstanding().add(request.getCurrentOutstanding()));
        member = persist(member, eventDate);
        eventService.addOutstandingUpdateEvent(member, eventDate, legacyOutstandingBefore, currentOutstandingBefore, request.getEventExtra());
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse updateMemberPresence(MemberRelatedRequest request) {
        ZonedDateTime eventDate = ZonedDateTime.now();
        Member member = getMember(request.getMemberId());
        member = member.updateLastPresenceDate(eventDate);
        member = persist(member, eventDate);
        eventService.addEvent(member.getId(), EventType.PRESENCE, eventDate, request.getEventExtra());
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse addJustifiedAbsence(MemberRelatedRequest request) {
        Member member = getMember(request.getMemberId());
        member = updateAbsences(member, EventType.ADD_JUSTIFY_ABSENCE, member.getAbsences(), member.getJustifiedAbsences() + 1, request.getEventExtra());
        return MemberRelatedResponse.builder().member(member).build();
    }

    public MemberRelatedResponse addAbsence(MemberRelatedRequest request) {
        Member member = getMember(request.getMemberId());
        member = updateAbsences(member, EventType.ADD_ABSENCE, member.getAbsences() + 1, member.getJustifiedAbsences(), request.getEventExtra());
        return MemberRelatedResponse.builder().member(member).build();
    }

    public Member applyStudioReportCharges(Member member, StudioReport studioReport) {
        member = member.moveCurrentOutstandingToLegacy();
        member = chargeService.applyStudioUsageCharge(member, studioReport);
        return memberRepository.save(member);
    }

    public MemberPaymentResponse applyMemberRepayment(MemberPaymentRequest request) {
        Member member = getMember(request.getMemberId());
        ZonedDateTime eventDate = ZonedDateTime.now();
        BigDecimal paymentAmount = request.getAmount();

        BigDecimal legacyOutstandingBefore = member.getLegacyOutstanding();
        BigDecimal currentOutstandingBefore = member.getCurrentOutstanding();
        BigDecimal totalOutstandingBefore = member.getTotalOutstanding();

        member = member
                .applyPayment(paymentAmount)
                .updateLastPresenceDate(eventDate);

        BigDecimal amountUsed = totalOutstandingBefore.subtract(member.getTotalOutstanding());
        BigDecimal amountLeft = paymentAmount.compareTo(amountUsed) > 0 ? paymentAmount.subtract(amountUsed) : BigDecimal.ZERO;

        eventService.addPaymentEvent(member, eventDate, paymentAmount, amountUsed, amountLeft,
                legacyOutstandingBefore, currentOutstandingBefore, request.getEventExtra());

        member = persist(member, eventDate);
        return MemberPaymentResponse.builder()
                .member(member)
                .amountUsed(amountUsed)
                .amountLeft(amountLeft)
                .build();
    }

    public List<Member> getMembers() {
        return memberRepository.getAllMembers();
    }

    public List<Member> getActiveMembers() {
        return memberRepository.findMembersByStatus(MemberStatus.ACTIVE);
    }

    public List<Integer> getIdsOfActiveMembers() {
        return memberRepository.findIdsOfActiveMembers();
    }

    public List<Member> getMembersToDeactivate() {
        List<Member> membersToDeactivate = new ArrayList<>();
        List<Member> activeMembers = getActiveMembers();
        for (Member member : activeMembers) {
            if (shouldDeactivateMember(member)) {
                membersToDeactivate.add(member);
            }
        }
        return membersToDeactivate;
    }

    public List<Member> getMembersNotPresentAfter(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        return memberRepository.findMembersByLastPresenceDateBefore(date.atStartOfDay(ZoneId.systemDefault()));
    }

    public Member getMember(int id) {
        lockDAO.lockMember(id);
        Optional<Member> memberOpt = memberRepository.findById(id);
        if (memberOpt.isPresent()) {
            return memberOpt.get();
        } else {
            throw new MaestrosLogicException("Member not found, id " + id);
        }
    }

    private Member assignCard(Member member, String accessCardId, ZonedDateTime eventDate, Map<String, Object> eventExtra) {
        member.setAccessCardId(accessCardId);
        eventService.addAccessCardAssignmentEvent(member, accessCardId, eventDate, eventExtra);
        member = chargeService.applyAccessCardCharge(member);
        return persist(member, eventDate);
    }

    public Member updateAbsences(Member member, EventType absenceEventType, int absences, int justifiedAbsences, Map<String, Object> payload) {
        ZonedDateTime eventDate = ZonedDateTime.now();
        member.setAbsences(absences);
        member.setJustifiedAbsences(justifiedAbsences);
        eventService.addAbsenceUpdateEvent(member, absenceEventType, eventDate, payload);
        return persist(member, eventDate);
    }

    private boolean shouldDeactivateMember(Member member) {
        return (member.getAbsences() > 0 && member.getJustifiedAbsences() == 0)
                || (member.getAbsences() > 1 && member.getJustifiedAbsences() > 0);
    }

    private Member persist(Member member, ZonedDateTime eventDate) {
        member.setLastUpdateDate(eventDate);
        return memberRepository.save(member);
    }
}
