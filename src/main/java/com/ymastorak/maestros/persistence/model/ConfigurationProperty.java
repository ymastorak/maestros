package com.ymastorak.maestros.persistence.model;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;

@Entity
@Table(name = "configuration")
@Data
@Accessors(chain = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationProperty {
    @Id
    private String name;
    @Column(nullable = false)
    private String value;
    @Column(nullable = false)
    private ZonedDateTime lastUpdated;
}
