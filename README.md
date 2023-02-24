Playing with OpenTelemetry Collector

# Running Collector

Simple case

```sh
docker run --rm \
           -v "$(pwd)/collector/config-logging.yml:/etc/otelcol/config.yaml" \
           -p "4317:4317" \
           otel/opentelemetry-collector:0.72.0
```
