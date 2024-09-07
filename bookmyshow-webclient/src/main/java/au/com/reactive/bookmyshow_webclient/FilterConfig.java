package au.com.reactive.bookmyshow_webclient;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class FilterConfig implements Filter {
    private final String CORRELATION_ID = "X-Correlation-Id";

    @Autowired
    private Tracer tracer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        Span span = this.tracer.nextSpan().name("customSpan");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(span.start())) {
            log.info("Should log with different span ID");
        }
        finally {
            span.end();
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        MDC.put("X-B3-SpanId", httpRequest.getHeader("X-B3-SpanId"));
        log.info("Filtering coming request and set MDC context information {}",httpRequest.getHeader("X-B3-SpanId"));
        // pass the request
        chain.doFilter(request, response);
    }
}
