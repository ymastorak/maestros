package com.ymastorak.maestros.persistence.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "events")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Integer memberId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType type;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Type(type = "jsonb")
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;
}
