package com.software.security.zeroday.security.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

@Component
public class SanitizationUtil {
    private final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("a", "p", "b", "i", "u", "strong", "em",
            "ul", "ol", "li", "table", "thead", "tbody", "tr", "td", "th")
        .allowAttributes("href").onElements("a")
        .requireRelNofollowOnLinks()
        .allowUrlProtocols("http", "https")
        .toFactory();

    public String sanitize(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        return POLICY.sanitize(content);
    }
}
