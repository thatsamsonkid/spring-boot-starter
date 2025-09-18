package io.unbyte.sandbox.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ArchUnit tests to enforce hexagonal architecture rules
 */
public class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .importPackages("io.unbyte.sandbox");
    }

    @ArchTest
    static ArchRule layeredArchitecture = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Domain").definedBy("..domain..")
                .layer("Application").definedBy("..application..")
                .layer("Infrastructure").definedBy("..infrastructure..")
                .layer("Shared").definedBy("..shared..")
                .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer();
        

    @Test
    void domainShouldNotHaveSpringAnnotations() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..domain..")
                .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Service.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Repository.class);
        
        rule.check(classes);
    }

    @Test
    void domainShouldNotDependOnSpring() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", 
                        "jakarta.persistence..", 
                        "com.fasterxml.jackson..");
        
        rule.check(classes);
    }

    @Test
    void applicationShouldNotDependOnInfrastructure() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");
        
        rule.check(classes);
    }

    @Test
    void domainRepositoriesShouldBeInterfaces() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..domain.repository..")
                .should().beInterfaces();
        
        rule.check(classes);
    }

    @Test
    void applicationPortsShouldBeInterfaces() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..application.port..")
                .should().beInterfaces();
        
        rule.check(classes);
    }

    @Test
    void noPublicFields() {
        ArchRule rule = ArchRuleDefinition.noFields()
                .that().areNotStatic()
                .and().areNotFinal()
                .should().bePublic();
        
        rule.check(classes);
    }

    @Test
    void controllersShouldBeInWebPackage() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage("..infrastructure.web..");
        
        rule.check(classes);
    }

    @Test
    void sharedShouldNotDependOnOtherLayers() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat().resideInAnyPackage("..domain..", "..application..", "..infrastructure..");
        
        rule.check(classes);
    }
}
