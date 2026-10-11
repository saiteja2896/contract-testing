package com.example.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;

    public ProductClient(RestTemplateBuilder builder,
                         @Value("${product-service.base-url}") String baseUrl) {
        this.restTemplate = builder.rootUri(baseUrl).build();
    }

    public Optional<Product> getProduct(long id) {
        try {
            return Optional.ofNullable(restTemplate.getForObject("/products/{id}", Product.class, id));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public List<Product> getAllProducts() {
        return restTemplate.exchange("/products", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Product>>() {
                }).getBody();
    }
}
