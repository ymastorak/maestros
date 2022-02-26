package com.ymastorak.maestros.service;

import com.ymastorak.maestros.persistence.model.ChargeType;
import com.ymastorak.maestros.persistence.model.Event;
import com.ymastorak.maestros.persistence.model.EventType;
import com.ymastorak.maestros.persistence.repository.EventRepository;
import com.ymastorak.maestros.persistence.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> getEventsByMemberId(int memberId) {
        return eventRepository.findEventsByMemberId(memberId);
    }

    public void addMemberEvent(Member member, EventType type, ZonedDateTime eventDate, Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("name", member.getName());
        payload.put("surname", member.getSurname());
        payload.put("phone", member.getPhone());
        payload.put("email", member.getEmail());
        addEvent(member.getId(), type, eventDate, payload);
    }

    public void addBlacklistEvent(Member member, String blacklistReason, ZonedDateTime eventDate, Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("blacklistReason", blacklistReason);
        addMemberEvent(member, EventType.BLACKLIST, eventDate, payload);
    }

    public void addAccessCardAssignmentEvent(Member member, String accessCardId, ZonedDateTime eventDate, Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("accessCardId", accessCardId);
        addMemberEvent(member, EventType.ACCESS_CARD_ASSIGNMENT, eventDate, payload);
    }

    public void addChargeEvent(Member member,
                               ZonedDateTime eventDate,
                               ChargeType type,
                               BigDecimal amount,
                               Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("chargeAmount", amount);
        payload.put("chargeType", type);
        payload.put("currentOutstandingAfter", member.getCurrentOutstanding());
        payload.put("totalOutstandingAfter", member.getTotalOutstanding());
        addEvent(member.getId(), EventType.CHARGE, eventDate, payload);
    }

    public void addPaymentEvent(Member member,
                                ZonedDateTime eventDate,
                                BigDecimal amount,
                                BigDecimal amountUsed,
                                BigDecimal amountLeft,
                                BigDecimal legacyOutstandingBefore,
                                BigDecimal currentOutstandingBefore,
                                Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("paymentAmount", amount);
        payload.put("amountUsed", amountUsed);
        payload.put("amountLeft", amountLeft);
        addOutstandingAlterationEvent(member, eventDate, EventType.PAYMENT, legacyOutstandingBefore, currentOutstandingBefore, payload);
    }

    public void addOutstandingUpdateEvent(Member member,
                                          ZonedDateTime eventDate,
                                          BigDecimal legacyOutstandingBefore,
                                          BigDecimal currentOutstandingBefore,
                                          Map<String, Object> payload) {
        addOutstandingAlterationEvent(member, eventDate, EventType.MANUAL_OUTSTANDING_UPDATE, legacyOutstandingBefore, currentOutstandingBefore, payload);
    }

    private void addOutstandingAlterationEvent(Member member,
                                              ZonedDateTime eventDate,
                                              EventType eventType,
                                              BigDecimal legacyOutstandingBefore,
                                              BigDecimal currentOutstandingBefore,
                                              Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("legacyOutstandingBefore", legacyOutstandingBefore);
        payload.put("legacyOutstandingAfter", member.getLegacyOutstanding());
        payload.put("currentOutstandingBefore", currentOutstandingBefore);
        payload.put("currentOutstandingAfter", member.getCurrentOutstanding());
        payload.put("totalOutstandingBefore", legacyOutstandingBefore.add(currentOutstandingBefore));
        payload.put("totalOutstandingAfter", member.getTotalOutstanding());
        addEvent(member.getId(), eventType, eventDate, payload);
    }

    public void addAbsenceUpdateEvent(Member member, EventType absenceEventType, ZonedDateTime eventDate, Map<String, Object> payload) {
        if (payload == null) {
            payload = new HashMap<>();
        }
        payload.put("absences", member.getAbsences());
        payload.put("justifiedAbsences", member.getJustifiedAbsences());
        addMemberEvent(member, absenceEventType, eventDate, payload);
    }

    public void addEvent(int memberId, EventType type, ZonedDateTime eventDate, Map<String, Object> payload) {
        Event event = Event.builder()
                .memberId(memberId)
                .type(type)
                .date(eventDate)
                .payload(payload)
                .build();
        eventRepository.save(event);
    }
}
