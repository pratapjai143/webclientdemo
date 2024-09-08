package au.com.reactive.bookmyshow_webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import reactor.core.publisher.Mono;

@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

    Logger log = LoggerFactory.getLogger(BookmyshowWebclientApplication.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestInterceptor());
    }

    @Autowired
    WebClient.Builder webClientBuilder;

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public WebClient webClient() {
        return webClientBuilder.baseUrl("http://localhost:9090/BookMyShow/Service")
                //.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest())
                .filter(logResponse()).build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clinetRequest -> {
            MDC.put("spanId", clinetRequest.headers().get("X-B3-SpanId").stream().findFirst().get());
            //log.info("Request {} {}", clinetRequest.method(), clinetRequest.url());
            //clinetRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clinetRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clinetResponse -> {
            //log.info("Response status code {} ", clinetResponse.statusCode());
            //clinetResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clinetResponse);
        });
    }
}
