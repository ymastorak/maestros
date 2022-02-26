package com.ymastorak.maestros.service;

import com.ymastorak.maestros.persistence.DAO.LockDAO;
import com.ymastorak.maestros.persistence.model.ConfigurationProperty;
import com.ymastorak.maestros.persistence.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    public static final String MEETING_ATTENDANCE_CHARGE_PROPERTY_NAME = "meetingAttendanceCharge";
    public static final String MAX_ADMIN_USAGE_COST_PROPERTY_NAME = "maxAdminUsageCost";
    public static final String STUDIO_USAGE_COST_PER_HOUR_PROPERTY_NAME = "studioUsageCostPerHour";
    public static final String REGISTRATION_CHARGE_PROPERTY_NAME = "registrationCharge";
    public static final String REACTIVATION_CHARGE_PROPERTY_NAME = "reactivationCharge";
    public static final String ACCESS_CARD_CHARGE = "accessCardCharge";

    private final ConfigurationRepository configurationRepository;
    private final LockDAO lockDAO;

    @PostConstruct
    public void init() {
        createConfigurationPropertyIfDoesNotExist(MEETING_ATTENDANCE_CHARGE_PROPERTY_NAME, Integer.toString(1));
        createConfigurationPropertyIfDoesNotExist(MAX_ADMIN_USAGE_COST_PROPERTY_NAME, Integer.toString(3));
        createConfigurationPropertyIfDoesNotExist(STUDIO_USAGE_COST_PER_HOUR_PROPERTY_NAME, Integer.toString(1));
        createConfigurationPropertyIfDoesNotExist(REGISTRATION_CHARGE_PROPERTY_NAME, Integer.toString(1));
        createConfigurationPropertyIfDoesNotExist(REACTIVATION_CHARGE_PROPERTY_NAME, Integer.toString(2));
        createConfigurationPropertyIfDoesNotExist(ACCESS_CARD_CHARGE, Integer.toString(5));
    }

    public BigDecimal getBigDecimalProperty(String name) {
        return new BigDecimal(getConfigurationProperty(name).getValue());
    }

    public List<ConfigurationProperty> updateConfiguration(Map<String, String> updateValues) {
        lockDAO.lockConfiguration();

        List<ConfigurationProperty> configurationProperties = getConfiguration();
        for (String name : updateValues.keySet()) {
            String value = updateValues.get(name);
            ConfigurationProperty property = findConfigurationProperty(name, configurationProperties);
            if (property != null) {
                property.setLastUpdated(ZonedDateTime.now());
                property.setValue(value);
                configurationRepository.save(property);
            } else {
                throw new MaestrosLogicException("Configuration property not found: "+name);
            }
        }
        return configurationProperties;
    }

    public List<ConfigurationProperty> getConfiguration() {
        return configurationRepository.findAll();
    }

    private ConfigurationProperty getConfigurationProperty(String name) {
        return configurationRepository.getByName(name).orElseThrow(() -> new MaestrosLogicException("Configuration property not found: "+name));
    }

    private void createConfigurationPropertyIfDoesNotExist(String name, String value) {
        Optional<ConfigurationProperty> propertyOpt = configurationRepository.getByName(name);
        if (!propertyOpt.isPresent()) {
            ConfigurationProperty property = ConfigurationProperty.builder()
                    .name(name)
                    .value(value)
                    .lastUpdated(ZonedDateTime.now())
                    .build();
            configurationRepository.save(property);
        }
    }

    private ConfigurationProperty findConfigurationProperty(String name, List<ConfigurationProperty> properties) {
        for (ConfigurationProperty property : properties) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }
}
