package com.thepracticaldeveloper.reactivebackend.controller;





import com.thepracticaldeveloper.reactivebackend.configuration.QuijoteDataLoader;
import com.thepracticaldeveloper.reactivebackend.domain.Quote;
import com.thepracticaldeveloper.reactivebackend.repository.QuoteMongoBlockingRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuoteBlockingControllerIntegrationTest {

    @MockBean
    private QuoteMongoBlockingRepository quoteMongoBlockingRepository;

    // This one is not needed, but we need to override the real one to prevent the default behavior
    @MockBean
    private QuijoteDataLoader quijoteDataLoader;

    @LocalServerPort
    private int serverPort;

    private RestTemplate restTemplate;

    private String serverBaseUrl;

    private List<Quote> quoteList;

   @BeforeEach
    public void setUp() {
        serverBaseUrl = "http://localhost:" + serverPort;
        restTemplate = new RestTemplate();
        quoteList = Lists.newArrayList(new Quote("1", "mock-book", "Quote 1"),
                new Quote("2", "mock-book", "Quote 2"),
                new Quote("3", "mock-book", "Quote 3"),
                new Quote("4", "mock-book", "Quote 4"));
    }

    @Test
    public void simpleGetRequest() {
        // given
        given(quoteMongoBlockingRepository.findAll()).willReturn(quoteList);

        // when
        ResponseEntity<List<Quote>> receivedQuoteList = restTemplate.exchange(serverBaseUrl + "/quotes-blocking",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Quote>>() {
                });

        // then
        assertThat(receivedQuoteList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(receivedQuoteList.getBody()).isEqualTo(
                Lists.newArrayList(new Quote("1", "mock-book", "Quote 1"),
                        new Quote("2", "mock-book", "Quote 2"),
                        new Quote("3", "mock-book", "Quote 3"),
                        new Quote("4", "mock-book", "Quote 4")));
    }

    @Test
    public void pagedGetRequest() {
        // given
        given(quoteMongoBlockingRepository.findAllByIdNotNullOrderByIdAsc(PageRequest.of(1, 2)))
                .willReturn(quoteList.subList(0, 2));

        // when
        ResponseEntity<List<Quote>> receivedQuoteList = restTemplate.exchange(
                serverBaseUrl + "/quotes-blocking-paged?page=1&size=2",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Quote>>() {
                });

        // then
        assertThat(receivedQuoteList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(receivedQuoteList.getBody()).isEqualTo(
                Lists.newArrayList(new Quote("1", "mock-book", "Quote 1"),
                        new Quote("2", "mock-book", "Quote 2")));
    }
}
