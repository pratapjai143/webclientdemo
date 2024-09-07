package au.com.reactive.bookmyshow_webclient;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    private Tracer tracer;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userId = request.getHeader("userId");
        //Span currentSpan = tracer.currentSpan();
        //String correlationId = currentSpan.context().traceId();
        //MDC.put("spanId", UUID.randomUUID().toString());
        MDC.put("transaction.id", "wrwrewr");
        MDC.put("transaction.owner", "345345345");
        //log.info("Intercept coming request and set MDC context information {}", correlationId);
        MDC.put("userId", "pratap232w");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove("userId");
    }
}