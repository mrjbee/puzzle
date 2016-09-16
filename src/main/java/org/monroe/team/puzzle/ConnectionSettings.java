package org.monroe.team.puzzle;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

//@Component
@ConfigurationProperties(prefix="person", ignoreUnknownFields = false)
public class ConnectionSettings {

    @NotNull
    private String firstName;

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

}