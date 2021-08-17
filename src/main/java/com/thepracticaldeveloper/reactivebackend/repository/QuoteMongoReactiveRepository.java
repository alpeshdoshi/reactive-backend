package com.thepracticaldeveloper.reactivebackend.repository;

import com.thepracticaldeveloper.reactivebackend.domain.Quote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

//ReactiveSortingRepository, adds paging and sorting functionality on top of the basic ReactiveCrudRepository
public interface QuoteMongoReactiveRepository extends ReactiveSortingRepository<Quote, String> {
    // Pageable argument is to define the offset and the results per page
    Flux<Quote> findAllByIdNotNullOrderByIdAsc(final Pageable page);
}
