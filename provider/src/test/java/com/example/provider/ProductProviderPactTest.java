package com.example.provider;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@Provider("ProductService")
@PactBroker // host, port, scheme and token come from pactbroker.* system properties (see provider/pom.xml)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductProviderPactTest {

    @LocalServerPort
    int port;

    @Autowired
    ProductRepository repository;

    @BeforeEach
    void setUp(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("product 1 exists")
    void product1Exists() {
        repository.clear();
        repository.save(new Product(1L, "Laptop", 999.99));
    }

    @State("product 999 does not exist")
    void product999DoesNotExist() {
        repository.clear();
    }

    @State("products exist")
    void productsExist() {
        repository.clear();
        repository.save(new Product(1L, "Laptop", 999.99));
        repository.save(new Product(2L, "Mouse", 19.99));
    }
}
