Playing with OpenTelemetry Collector

# Running Test App

The test app expects an OTLP listener on `localhost:4317`.

```sh
./gradlew run
```

# Running Collector

## Exporting to Console

```sh
docker run --rm \
           -v "$(pwd)/collector/config-logging.yml:/etc/otelcol/config.yaml" \
           -p "4317:4317" \
           otel/opentelemetry-collector:0.72.0
```

## Exporting to Google Cloud Platform Operations Suite

This requires some preparation:

```sh
export GOOGLE_CLOUD_PROJECT="$(gcloud config get project)"
gcloud iam service-accounts create "otel-collector-sa" \
    --description="This SA runs the OpenTelemetry Collector" \
    --display-name="OpenTelemetry Collector SA"

gcloud projects add-iam-policy-binding "${GOOGLE_CLOUD_PROJECT}" \
    --member="serviceAccount:otel-collector-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com" \
    --role="roles/cloudtrace.agent"

gcloud projects add-iam-policy-binding "${GOOGLE_CLOUD_PROJECT}" \
    --member="serviceAccount:otel-collector-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com" \
    --role="roles/monitoring.metricWriter"

(umask 077 &&
 gcloud iam service-accounts keys create \
   --iam-account="otel-collector-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com" \
   otel-collector-sa.json)
```

Finally, to run the collector:

```sh
docker run --rm \
           --volume "$(pwd)/collector/config-gcp.yml:/etc/otelcol-contrib/config.yaml" \
           --volume "$(pwd)/otel-collector-sa.json:/etc/otel/key.json" \
           --env GOOGLE_APPLICATION_CREDENTIALS=/etc/otel/key.json \
           -p "4317:4317" \
           otel/opentelemetry-collector-contrib:0.72.0
```

# Clean Up

Disable the service account key:

```sh
gcloud iam service-accounts keys disable "$(jq -r .private_key_id otel-collector-sa.json)" \
    --iam-account="otel-collector-sa@${GOOGLE_CLOUD_PROJECT}.iam.gserviceaccount.com"
```
