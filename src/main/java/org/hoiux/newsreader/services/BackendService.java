package org.hoiux.newsreader.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hoiux.newsreader.data.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
  The service that gives access to the back end REST API.
 */
@SuppressWarnings("Serial")
@Service
public class BackendService implements Serializable {

    private final WebClient webClient;

    public BackendService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/v1/newsreader")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public User getUser(String username) {

        User user = webClient.get().uri("/user/" + username)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        return user;
    }

    public List<Category> getCategories() {

        List<Category> categories = webClient.get().uri("/category")
                .retrieve()
                .toEntityList(Category.class)
                .block()
                .getBody();

        return categories;
    }

    public void addNewFeed(String rss) {

        if (rss.isBlank()) {
            return;
        }

        String json = String.format("{ \"rss_source\": \"%s\" }", rss);

        try {
            webClient.post()
                    .uri("/feed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .toEntity(String.class)
                    .toFuture()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public FeedCollection getChannels() {

        ResponseEntity<String> response = webClient.get().uri("/feed")
                .retrieve()
                .toEntity(String.class)
                .block();

        String c = response.getBody();

        FeedCollection feedCollection = null;

        ObjectMapper mapper = new ObjectMapper();
        try {
            feedCollection = mapper.readValue(c, FeedCollection.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return feedCollection;
    }

    public FeedCollection refreshFeed(String rssSource) {

        String json = String.format("{ \"rss_source\": \"%s\" }", rssSource);

        try {
            ResponseEntity<String> response = webClient.put()
                    .uri("/feed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(json)
                    .retrieve()
                    .toEntity(String.class)
                    .toFuture()
                    .get();

            String c = response.getBody();

            FeedCollection feedCollection = null;

            ObjectMapper mapper = new ObjectMapper();

            feedCollection = mapper.readValue(c, FeedCollection.class);

            return feedCollection;

        } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public FeedCollection refreshAllFeeds() {

        // Get existing feeds.
        FeedCollection feeds = getChannels();

        // Refresh each channel.
        for (Channel channel: feeds.getFeeds()) {
            refreshFeed(channel.getSource());
        }

        // Get all feeds again and return the collection.
        return getChannels();
    }

    public Channel getSingleChannel(Long id) {
        ResponseEntity<Channel> response = webClient.get().uri("/feed/" + id.toString())
                .retrieve()
                .toEntity(Channel.class)
                .block();
        return response.getBody();
    }

    public String getFilters() {

        List<Filter> response = webClient
                .get()
                .uri("/filter")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Filter>>() {
                })
                .block();

        String allFilters = "";
        int i = 1;
        for (Filter filter : response) {
            allFilters += filter.getContent();
            if (i < response.size()) {
                allFilters += "\n";
            }
        }

        return allFilters;
    }

    public void applyFilters(String filters) {

        // delete all existing filters first
        List<Filter> response = webClient
                .get()
                .uri("/filter")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Filter>>() {
                })
                .block();

        for (Filter filter : response) {
            webClient.delete()
                    .uri("/filter/" + filter.getId())
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(responseEntity -> {
                    });
        }

        String[] lines = filters.split("\\n");
        List<String> lineList = java.util.Arrays.asList(lines);

        // add the new filters
        for (String filter : lineList) {
            if (!filter.isBlank()) {
                String json = String.format("{ \"content\": \"%s\" }", filter);
                webClient.post()
                        .uri("/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(json)
                        .retrieve()
                        .toBodilessEntity()
                        .subscribe(responseEntity -> {
                        });

            }
        }
    }
}