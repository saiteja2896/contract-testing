package com.example.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ProductService", pactVersion = PactSpecVersion.V3)
class ProductClientPactTest {

    private static final Map<String, String> JSON = Map.of("Content-Type", "application/json");

    @Pact(consumer = "ProductConsumer")
    RequestResponsePact productExists(PactDslWithProvider builder) {
        return builder
                .given("product 1 exists")
                .uponReceiving("a request for product 1")
                .path("/products/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(JSON)
                .body(new PactDslJsonBody()
                        .integerType("id", 1L)
                        .stringType("name", "Laptop")
                        .decimalType("price", 999.99))
                .toPact();
    }

    @Pact(consumer = "ProductConsumer")
    RequestResponsePact productDoesNotExist(PactDslWithProvider builder) {
        return builder
                .given("product 999 does not exist")
                .uponReceiving("a request for product 999")
                .path("/products/999")
                .method("GET")
                .willRespondWith()
                .status(404)
                .toPact();
    }

    @Pact(consumer = "ProductConsumer")
    RequestResponsePact listProducts(PactDslWithProvider builder) {
        DslPart body = PactDslJsonArray.arrayMinLike(1)
                .integerType("id", 1L)
                .stringType("name", "Laptop")
                .decimalType("price", 999.99)
                .closeObject();
        return builder
                .given("products exist")
                .uponReceiving("a request for all products")
                .path("/products")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(JSON)
                .body(body)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "productExists")
    void getsExistingProduct(MockServer mockServer) {
        Optional<Product> product = clientFor(mockServer).getProduct(1);

        assertThat(product).isPresent();
        assertThat(product.get().id()).isEqualTo(1L);
        assertThat(product.get().name()).isEqualTo("Laptop");
    }

    @Test
    @PactTestFor(pactMethod = "productDoesNotExist")
    void returnsEmptyForMissingProduct(MockServer mockServer) {
        assertThat(clientFor(mockServer).getProduct(999)).isEmpty();
    }

    @Test
    @PactTestFor(pactMethod = "listProducts")
    void listsProducts(MockServer mockServer) {
        List<Product> products = clientFor(mockServer).getAllProducts();

        assertThat(products).isNotEmpty();
        assertThat(products.get(0).name()).isEqualTo("Laptop");
    }

    private ProductClient clientFor(MockServer mockServer) {
        return new ProductClient(new RestTemplateBuilder(), mockServer.getUrl());
    }
}
