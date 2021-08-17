package com.thepracticaldeveloper.reactivebackend.controller;

import com.thepracticaldeveloper.reactivebackend.domain.Quote;
import com.thepracticaldeveloper.reactivebackend.repository.QuoteMongoReactiveRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
//it doesn’t matter if you use a reactive web approach in the backend; it won’t be really reactive and non-blocking unless your client can handle it as well.

@RestController
public class QuoteReactiveController {

    private static final int DELAY_PER_ITEM_MS = 100;

    private final QuoteMongoReactiveRepository quoteMongoReactiveRepository;

    public QuoteReactiveController(final QuoteMongoReactiveRepository quoteMongoReactiveRepository) {
        this.quoteMongoReactiveRepository = quoteMongoReactiveRepository;
    }

    @GetMapping("/quotes-reactive")
    public Flux<Quote> getQuoteFlux() {
        //every quote has a processing time of 100 milliseconds- to mimic overloaded server
        //Having a simulated delay will also help us visualize the differences between reactive and MVC strategies.
        //We’ll run the client and server on the same machine.
        //So, if we don’t introduce the delay, the response times will be so good that it will be hard to spot the differences.
        return quoteMongoReactiveRepository.findAll().delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }

    //to go full reactive and use the SSE support in Spring- we have to set (explicitly or implicitly) the Accept header to text/event-stream.. this will activate
    // reactive functionality in Spring to open an SSE channel and publish server to client events
    @GetMapping(value = "/quotes-reactive-paged")
    public Flux<Quote> getQuoteFlux(final @RequestParam(name = "page") int page,
                                    final @RequestParam(name = "size") int size) {
        return quoteMongoReactiveRepository.findAllByIdNotNullOrderByIdAsc(PageRequest.of(page, size))
                .delayElements(Duration.ofMillis(DELAY_PER_ITEM_MS));
    }
}
