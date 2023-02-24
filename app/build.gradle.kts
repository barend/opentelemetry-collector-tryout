plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation("io.opentelemetry:opentelemetry-api:1.23.1")
    implementation("io.opentelemetry:opentelemetry-sdk:1.23.1")
    implementation("io.opentelemetry:opentelemetry-semconv:1.23.1-alpha")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.23.1")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.9.1")
        }
    }
}

application {
    mainClass.set("io.github.barend.oteltryout.App")
}
