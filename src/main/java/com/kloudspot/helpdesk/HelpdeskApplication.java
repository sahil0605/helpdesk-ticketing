package com.kloudspot.helpdesk;

import com.kloudspot.helpdesk.auth.HelpdeskProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableConfigurationProperties(HelpdeskProperties.class)
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class HelpdeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelpdeskApplication.class, args);
    }
}
