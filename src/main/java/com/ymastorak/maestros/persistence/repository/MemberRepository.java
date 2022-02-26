package com.ymastorak.maestros.persistence.repository;

import com.ymastorak.maestros.persistence.model.Member;
import com.ymastorak.maestros.persistence.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    @Query("select m from Member m")
    List<Member> getAllMembers();

    List<Member> findMembersByStatus(MemberStatus status);

    List<Member> findMembersByLastPresenceDateBefore(ZonedDateTime date);

    Optional<Member> findMemberByAccessCardId(String cardId);

    @Query("select m.id from Member m where m.status = 'ACTIVE'")
    List<Integer> findIdsOfActiveMembers();
}
