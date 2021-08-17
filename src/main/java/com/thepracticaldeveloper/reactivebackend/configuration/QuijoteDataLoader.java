package com.thepracticaldeveloper.reactivebackend.configuration;

import com.thepracticaldeveloper.reactivebackend.domain.Quote;
import com.thepracticaldeveloper.reactivebackend.repository.QuoteMongoReactiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Supplier;

@Component
//example of how Fluxes work in a blocking programming style.
//application runner will read the text version of the book and store them in mongo db the first time the application runs
//ApplicationRunner interface is not prepared for a reactive approach
/*
The data loader is a good example of using a reactive programming style with a blocking logic and self-subscription, as the ApplicationRunner interface is not prepared for a reactive approach:
    1. Since the repository is reactive, we need to block() to wait for the result of the one-element publisher (Mono) containing the number of quotes in the repository (the count method).
    2. We apply a reactive pattern to subscribe to the result of save() from the reactive repository. Remember that, if we don’t consume the result, the quote is not stored.
    If the ApplicationRunner interface offered a reactive signature, meaning a Flux or Mono return type, we could subscribe to the count() method instead and chain the Mono and Fluxes.
    Instead, we need to call block() in our example to keep the runner executor thread alive. Otherwise, we wouldn’t be able to load the data before the executor finished.
    In the loader case, it would make more sense to switch to a purely functional approach with the classic repository.
    However, we used this as an example of how Fluxes work in a blocking programming style.
 */
public class QuijoteDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(QuijoteDataLoader.class);

    private final QuoteMongoReactiveRepository quoteMongoReactiveRepository;

    QuijoteDataLoader(final QuoteMongoReactiveRepository quoteMongoReactiveRepository) {
        this.quoteMongoReactiveRepository = quoteMongoReactiveRepository;
    }

    @Override
    public void run(final ApplicationArguments args) {
        //first check if the data is already there - quoteMongoReactiveRepository.count().block() == 0L if data is not there then create a flux from buffered reader
        //we need to block() to wait for the result of the Mono containing the number of quotes in repo
        if (quoteMongoReactiveRepository.count().block() == 0L) {
            //For the identifiers, use a functional Supplier interface to generate a sequence.
            var idSupplier = getIdSequenceSupplier();
            var bufferedReader = new BufferedReader(
                    new InputStreamReader(getClass()
                            .getClassLoader()
                            .getResourceAsStream("pg2000.txt"))
            );
            //if we don’t consume the result (subscribe), the quote is not stored.
            Flux.fromStream(
                    bufferedReader.lines()
                            .filter(l -> !l.trim().isEmpty())
                            //for each line, convert it to a Quote object and store it in the database
                            .map(l -> quoteMongoReactiveRepository.save(
                                    new Quote(idSupplier.get(),
                                            "El Quijote", l))
                            )
            ).subscribe(m -> log.info("New quote loaded: {}", m.block()));
            log.info("Repository contains now {} entries.",
                    quoteMongoReactiveRepository.count().block());
        }
    }

    private Supplier<String> getIdSequenceSupplier() {
        return new Supplier<>() {
            Long l = 0L;

            @Override
            public String get() {
                // adds padding zeroes
                return String.format("%05d", l++);
            }
        };
    }
}
