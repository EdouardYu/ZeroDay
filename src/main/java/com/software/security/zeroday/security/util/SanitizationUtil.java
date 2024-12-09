package com.software.security.zeroday.security.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SanitizationUtil {
    private final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("a", "p", "b", "i", "u", "strong", "em",
            "ul", "ol", "li", "table", "thead", "tbody", "tr", "td", "th",
            "h1", "h2", "h3", "h4", "h5", "h6")
        .allowAttributes("href").onElements("a")
        .requireRelNofollowOnLinks()
        .allowUrlProtocols("http", "https")
        .toFactory();

    public String sanitizeString(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        return this.POLICY.sanitize(content);
    }

    private final PolicyFactory SVG_POLICY = new HtmlPolicyBuilder()
        .allowElements("svg", "g", "path", "rect", "circle", "ellipse", "line",
            "polyline", "polygon", "text", "tspan", "image", "title", "desc")
        .allowAttributes("width", "height", "x", "y", "rx", "ry", "fill", "stroke",
            "viewBox", "xmlns", "xmlns:xlink", "d", "href", "transform")
        .onElements("svg", "path", "rect", "circle", "ellipse", "line",
            "polyline", "polygon", "text", "tspan", "image")
        .allowAttributes("xlink:href").onElements("image")
        .allowUrlProtocols("http", "https", "data")
        .toFactory();

    public String sanitizeSvg(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            return this.SVG_POLICY.sanitize(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the SVG file", e);
        }
    }
}
