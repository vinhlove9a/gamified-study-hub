package com.gamifiedstudyhub.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith boundary tooling. Tagged {@code integration} so it is skipped by the
 * default {@code mvn test} (the package structure has not been annotated into modules
 * yet, so {@code verify()} would report violations). Run with {@code mvn -Pintegration test}
 * to inspect the current module graph and generate C4/PlantUML docs under
 * {@code target/spring-modulith-docs}.
 */
@Tag("integration")
class ModularityTests {

    private final ApplicationModules modules = ApplicationModules.of(GamifiedStudyHubBackendApplication.class);

    @Test
    void verifyModuleStructure() {
        modules.verify();
    }

    @Test
    void writeDocumentation() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}
