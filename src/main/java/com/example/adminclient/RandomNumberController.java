package com.example.adminclient;


import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;

@RestController
public class RandomNumberController {

    private static final Logger logger = LoggerFactory.getLogger(RandomNumberController.class);


    private final WebClient webClient;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    public RandomNumberController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:5001")
                .build();
    }

    //    Blocking method
    @Observed(name = "my.controller.random-blocking")
    @GetMapping(value = "/proxy/random", produces = "text/plain")
    public String getRandomNumberBlocking() {
        return restTemplate.getForObject("http://localhost:5001/random", String.class);
    }

    @GetMapping("/proxy/random-mono")
//    "public Integer getRandomNumber()" Worked until I removed spring-boot-starter-web
    public Mono<Integer> getRandomNumber() {
        return webClient.get()
                .uri("/random-mono")
                .retrieve()
                .bodyToMono(Integer.class);
    }

    @GetMapping("/proxy/random-flux")
    public Flux<Long> streamRandomNumbers(@RequestParam long timeoutSec, @RequestParam(defaultValue = "100") int limit) {

        innerMethodCall();

        return webClient.get()
                .uri("/random-flux")
                .retrieve()
                .bodyToFlux(Long.class);
//                .take(Duration.ofMillis(timeoutSec == 0 ? 10 : timeoutSec))
//                .take(limit)
//                .log();
//                .block(); // It throws exception that blocking operations for 'public List<Long> is not supported"
    }

    private void innerMethodCall() {
        try {
            Thread.sleep(random.nextInt(0, 100));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("method innerMethodCall()");
    }
}
