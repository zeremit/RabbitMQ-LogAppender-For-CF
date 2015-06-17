package com.kharevich.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentComponent {
    public EnvironmentComponent() {

    }

    @Value("#{systemEnvironment['username']}")
    private String userName;


    public String getUserName() {
        return userName;
    }
}
