package io.github.barend.oteltryout;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class App {
    private final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try (var otel = App.initOpenTelemetry()){
            new App().run();
        }
    }

    private static OpenTelemetrySdk initOpenTelemetry() {
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "collector-tryout")));

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
                .setResource(resource)
                .build();

        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
                .setResource(resource)
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setMeterProvider(sdkMeterProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
    }

    private void run() {
        var otel = GlobalOpenTelemetry.get();
        var tracer = otel.getTracer("collector-tryout");

        logger.info("Started.");
        var rootSpan = tracer.spanBuilder("test")
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SemanticAttributes.THREAD_NAME, Thread.currentThread().getName())
                .setAttribute(AttributeKey.stringKey("bogus-attribute"), UUID.randomUUID().toString())
                .startSpan();

        try (var ignored = rootSpan.makeCurrent()) {
            long sleepTime = 125L + (long)(new Random().nextDouble() * 250L);
            Thread.sleep(sleepTime);
            rootSpan.end();
            logger.info("Finished OK.");
        } catch (InterruptedException ingored) {
            // ignored
        }
    }
}
