package com.helloworld;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class HelloController {

    private final RestTemplate restTemplate;

    public HelloController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${downstream.url:http://hello-world-1:5000/internal}")
    private String downstreamUrl;

    @GetMapping("/")
    @CircuitBreaker(name = "downstream", fallbackMethod = "helloFallback")
    public String hello() {
        String response = restTemplate.getForObject(downstreamUrl, String.class);
        return "✨ Service A says: \"" + response + "\" — fresh from the demo pipeline, hot off the wire! 🚀";
    }

    /** Fallback when downstream is unavailable or circuit is open (fails fast at scale). */
    public String helloFallback(Throwable t) {
        return "✨ Service A is up! (Downstream at " + downstreamUrl + " is unreachable or circuit open — run it or set downstream.url for full demo.) 🚀";
    }
}
