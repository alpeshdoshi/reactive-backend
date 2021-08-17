package com.thepracticaldeveloper.reactivebackend.repository;

import com.thepracticaldeveloper.reactivebackend.domain.Quote;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

//PagingAndSortingRepository, adds paging and sorting functionality on top of the basic CrudRepository
public interface QuoteMongoBlockingRepository extends PagingAndSortingRepository<Quote, String> {
    List<Quote> findAllByIdNotNullOrderByIdAsc(final Pageable page);
}
