package com.kloudspot.helpdesk.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@ConfigurationProperties(prefix = "helpdesk")
public class HelpdeskProperties {

    private List<String> companyEmailDomains = new ArrayList<>(List.of("kloudspot.in"));

    public boolean isCompanyEmail(String email) {
        String domain = emailDomain(email);
        return domain != null && companyEmailDomains.stream()
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(domain::equals);
    }

    public static String emailDomain(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
    }
}
