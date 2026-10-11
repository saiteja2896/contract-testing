# pact-demo

Consumer-driven contract testing with [Pact](https://docs.pact.io) and **PactFlow** (`https://ever.pactflow.io`) on Spring Boot 3.3 / Java 17.

| Module | Role | What it does |
|---|---|---|
| `consumer` | `ProductConsumer` | `ProductClient` calls the product API. `ProductClientPactTest` runs against a Pact mock server and writes `pacts/ProductConsumer-ProductService.json`. |
| `provider` | `ProductService` | REST API (`GET /products`, `GET /products/{id}`). `ProductProviderPactTest` pulls the pacts from PactFlow, replays them against the real app and publishes the verification result. |

## 1. Set your PactFlow token (never commit it)

The token is read from the `PACT_BROKER_TOKEN` environment variable.

**Windows PowerShell**
```powershell
$env:PACT_BROKER_TOKEN = "<your read/write token>"
```

**Windows cmd**
```bat
set PACT_BROKER_TOKEN=<your read/write token>
```

**macOS / Linux**
```bash
export PACT_BROKER_TOKEN="<your read/write token>"
```

(Use a PactFlow *read/write* API token: Settings -> API Tokens.)

## 2. Run

```bash
# a) consumer: run tests -> generates ./pacts/ProductConsumer-ProductService.json
mvn -pl consumer test

# b) consumer: publish the pact to PactFlow
mvn -pl consumer pact:publish

# c) provider: fetch pacts from PactFlow, verify, publish results
mvn -pl provider test
```

Then open https://ever.pactflow.io and you should see **ProductConsumer -> ProductService** with a verified status.

Verify without publishing results:
```bash
mvn -pl provider test -Dpact.publish.results=false
```

## Where the settings live

- Broker URL / host: `pom.xml` (`pactbroker.url`, `pactbroker.host`)
- Publishing (consumer): Pact Maven plugin in `consumer/pom.xml`
- Verification (provider): `@PactBroker` in `ProductProviderPactTest` + surefire system properties in `provider/pom.xml`
- Pact version published = Maven project version (`0.0.1-SNAPSHOT`). Bump it (or use a git SHA) per build in real projects.

## CI

`.github/workflows/pact.yml` runs consumer test -> publish -> provider verification. Add a repository secret named `PACT_BROKER_TOKEN`.

## Troubleshooting

- **401/403 from PactFlow**: token missing or not read/write. Check `echo $env:PACT_BROKER_TOKEN`.
- **Provider finds no pacts**: run steps a and b first.
- **Behind a corporate proxy**: set `-Dhttps.proxyHost` / `-Dhttps.proxyPort` via `MAVEN_OPTS`.
