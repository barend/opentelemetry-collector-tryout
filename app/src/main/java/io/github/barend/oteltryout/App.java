package io.github.barend.oteltryout;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

public class App {
    private final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        new App().run();
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
