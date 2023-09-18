package commerce.emmerce.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class TestController {

    @GetMapping("/hello")
    public Flux<String> test() {
        return Flux.just("Hello ", "World");
    }

}
