package io.unbyte.sandbox.architecture;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

/**
 * Comprehensive ArchUnit tests to enforce hexagonal architecture rules
 */
@AnalyzeClasses(
        packages = "io.unbyte.sandbox",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    private static final String DOMAIN = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String INFRASTRUCTURE = "..infrastructure..";
    private static final String WEB = "..infrastructure.web..";
    private static final String PERSISTENCE = "..infrastructure.persistence..";
    private static final String EXTERNAL = "..infrastructure.external..";
    private static final String SHARED = "..shared..";

    /**
     * Enforces the layered architecture rules for hexagonal architecture
     */
    @ArchTest
    static final ArchRule layers =
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain")
                    .definedBy(DOMAIN)
                    .layer("Application")
                    .definedBy(APPLICATION)
                    .layer("Infrastructure")
                    .definedBy(INFRASTRUCTURE)
                    .layer("Web")
                    .definedBy(WEB)
                    //     .layer("Persistence").definedBy(PERSISTENCE)
                    //     .layer("External").definedBy(EXTERNAL)
                    .layer("Shared")
                    .definedBy(SHARED)
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Infrastructure")
                    .whereLayer("Infrastructure")
                    .mayNotBeAccessedByAnyLayer();

    /**
     * Domain layer must be pure - no external framework dependencies
     */
    @ArchTest
    static final ArchRule domainIsPure =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "com.fasterxml.jackson..",
                            "tools.jackson..",
                            "jakarta.validation..",
                            "org.hibernate..",
                            "io.swagger.v3..",
                            "org.springdoc..");

    /**
     * Domain layer should not have Spring annotations
     */
    @ArchTest
    static final ArchRule domainShouldNotHaveSpringAnnotations =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(DOMAIN)
                    .should()
                    .beAnnotatedWith(org.springframework.stereotype.Component.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.stereotype.Service.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.stereotype.Repository.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class);

    /**
     * Application layer should not depend on infrastructure
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnInfrastructure =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage(INFRASTRUCTURE);

    /**
     * Application layer must be framework-agnostic - no reactive types in use cases
     * Exception: Port interfaces can use reactive types for non-blocking behavior
     */
    @ArchTest
    static final ArchRule applicationUseCasesShouldNotDependOnReactiveTypes =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage("..application.usecase..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "reactor.core..", "reactor.util..", "org.reactivestreams..");

    /**
     * Application layer must be framework-agnostic - no Spring WebFlux
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnWebFlux =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web.reactive..",
                            "org.springframework.web.reactive.function..");

    /**
     * Application layer must be framework-agnostic - no Spring annotations
     */
    @ArchTest
    static final ArchRule applicationShouldNotHaveSpringAnnotations =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .beAnnotatedWith(org.springframework.stereotype.Component.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.stereotype.Service.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                    .orShould()
                    .beAnnotatedWith(org.springframework.context.annotation.Configuration.class);

    /**
     * Application layer must be framework-agnostic - no Jackson annotations
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnJackson =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("com.fasterxml.jackson..", "tools.jackson..");

    /**
     * Application layer must be framework-agnostic - no validation annotations
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnValidation =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("jakarta.validation..", "javax.validation..");

    /**
     * Application layer must be framework-agnostic - no persistence annotations
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnPersistence =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..", "javax.persistence..", "org.hibernate..");

    /**
     * Application layer must be framework-agnostic - no HTTP annotations
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnHttp =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http..",
                            "jakarta.servlet..",
                            "javax.servlet..");

    /**
     * Application layer must be framework-agnostic - no Micrometer
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnMicrometer =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("io.micrometer..");

    /**
     * Application layer must be framework-agnostic - no MapStruct
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnMapStruct =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.mapstruct..");

    /**
     * Application layer must be framework-agnostic - no OpenAPI/Swagger
     */
    @ArchTest
    static final ArchRule applicationShouldNotDependOnOpenApi =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(APPLICATION)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("io.swagger.v3..", "org.springdoc..");

    /**
     * Controllers may only depend on application + domain + shared (not persistence/entities directly)
     */
    @ArchTest
    static final ArchRule controllersOnlyCallUseCases =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage(WEB)
                    .and()
                    .areNotAnnotatedWith(org.junit.jupiter.api.Test.class)
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            "java..",
                            "jakarta.validation..",
                            "org.springframework.web..",
                            "org.springframework.http..",
                            "org.springframework.context..",
                            "org.springframework.core..",
                            "org.springframework.stereotype..",
                            "org.springframework.beans.factory.annotation..", // Added for
                            // @Autowired
                            "com.fasterxml.jackson..",
                            "tools.jackson..",
                            "io.micrometer.core.instrument..",
                            "io.swagger.v3.oas.annotations..",
                            "io.swagger.v3.oas.models..",
                            "org.springdoc..",
                            "reactor.core..",
                            "reactor.util..",
                            "org.reactivestreams..",
                            "org.slf4j..",
                            "jakarta.annotation..",
                            "org.mapstruct..", // Added for MapStruct mappers
                            DOMAIN,
                            APPLICATION,
                            WEB,
                            SHARED);

    /**
     * Infrastructure adapters should implement application ports
     */
    @ArchTest
    static final ArchRule infrastructureAdaptersShouldImplementPorts =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..infrastructure.adapter..")
                    .and()
                    .areNotInterfaces()
                    .should()
                    .implement(io.unbyte.sandbox.application.port.Port.class)
                    .allowEmptyShould(true);

    /**
     * Domain repositories should be interfaces
     */
    @ArchTest
    static final ArchRule domainRepositoriesShouldBeInterfaces =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..domain.repository..")
                    .should()
                    .beInterfaces();

    /**
     * Application ports should be interfaces
     */
    @ArchTest
    static final ArchRule applicationPortsShouldBeInterfaces =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..application.port..")
                    .should()
                    .beInterfaces();

    /**
     * No public fields allowed (encapsulation)
     */
    @ArchTest
    static final ArchRule noPublicFields =
            ArchRuleDefinition.noFields()
                    .that()
                    .areNotStatic()
                    .and()
                    .areNotFinal()
                    .should()
                    .bePublic();

    /**
     * Controllers should be in web package
     */
    @ArchTest
    static final ArchRule controllersShouldBeInWebPackage =
            ArchRuleDefinition.classes()
                    .that()
                    .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .should()
                    .resideInAPackage(WEB);

    /**
     * Shared layer should not depend on other layers
     */
    @ArchTest
    static final ArchRule sharedShouldNotDependOnOtherLayers =
            ArchRuleDefinition.noClasses()
                    .that()
                    .resideInAPackage(SHARED)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(DOMAIN, APPLICATION, INFRASTRUCTURE);

    /**
     * Shared utilities should have only private constructors
     */
    @ArchTest
    static final ArchRule sharedUtilitiesShouldHavePrivateConstructors =
            ArchRuleDefinition.classes()
                    .that()
                    .resideInAPackage("..shared.util..")
                    .and()
                    .areNotInterfaces()
                    .should()
                    .haveOnlyPrivateConstructors();

    /**
     * No cyclic dependencies between layers
     */
    @ArchTest
    static final ArchRule noCyclicDependencies =
            com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                    .matching("io.unbyte.sandbox.(*)..")
                    .should()
                    .beFreeOfCycles();
}
