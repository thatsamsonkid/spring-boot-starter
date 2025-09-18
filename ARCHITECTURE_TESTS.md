# Architecture Tests with ArchUnit

This document describes the comprehensive ArchUnit tests that enforce hexagonal architecture rules in the Sandbox project.

## Overview

ArchUnit tests ensure that the hexagonal architecture principles are maintained throughout the development lifecycle. These tests run as part of the test suite and will fail if architectural violations are detected.

## Test Structure

```
src/test/java/io/unbyte/sandbox/architecture/
├── HexagonalArchitectureTest.java          # Main test class
└── rules/
    ├── DomainLayerRules.java              # Domain layer rules
    ├── ApplicationLayerRules.java         # Application layer rules
    ├── InfrastructureLayerRules.java      # Infrastructure layer rules
    ├── SharedLayerRules.java              # Shared layer rules
    └── GeneralArchitectureRules.java       # General architecture rules
```

## Layer-Specific Rules

### Domain Layer Rules

1. **Domain Independence**

   - Domain layer should not depend on other layers
   - Domain layer should not have Spring annotations
   - Domain layer should not have external dependencies

2. **Domain Organization**

   - Domain entities should be in model package
   - Domain repositories should be interfaces
   - Domain services should be in service package

3. **Domain Purity**
   - Domain layer should be framework-agnostic
   - Domain layer should contain only business logic

### Application Layer Rules

1. **Application Dependencies**

   - Application layer should not depend on infrastructure
   - Application layer should only depend on domain and shared layers
   - Application layer should not have web-specific annotations

2. **Application Organization**

   - Application ports should be interfaces
   - Application services should be in service package
   - Application DTOs should be in dto package

3. **Application Services**
   - Application services should be annotated with @Service
   - Application services should orchestrate domain objects

### Infrastructure Layer Rules

1. **Infrastructure Implementation**

   - Infrastructure should implement application ports
   - Infrastructure adapters should be in adapter package
   - Infrastructure controllers should be in web package

2. **Infrastructure Dependencies**

   - Infrastructure should not depend on domain directly
   - Infrastructure should not contain business logic

3. **Infrastructure Annotations**
   - Infrastructure repositories should be annotated with @Repository
   - Infrastructure services should be annotated with @Service
   - Infrastructure controllers should be annotated with @RestController

### Shared Layer Rules

1. **Shared Independence**

   - Shared layer should not depend on other layers
   - Shared layer should not have Spring annotations
   - Shared layer should have minimal external dependencies

2. **Shared Organization**
   - Shared utilities should be static
   - Shared constants should be final
   - Shared DTOs should be in dto package
   - Shared exceptions should be in exception package

## General Architecture Rules

1. **Code Quality**

   - Classes should have meaningful names
   - No public fields allowed
   - No cyclic dependencies
   - No empty catch blocks

2. **Layer Separation**

   - Controllers should not have business logic
   - Services should be properly annotated
   - Repositories should be properly annotated

3. **API Design**
   - No deprecated methods in public APIs
   - Constants should be in UPPER_CASE
   - Public methods should have proper annotations

## Running Architecture Tests

### Maven

```bash
mvn test
```

### Specific Architecture Tests

```bash
mvn test -Dtest=HexagonalArchitectureTest
```

### IDE

Run the `HexagonalArchitectureTest` class directly in your IDE.

## Test Results

When architecture violations are detected, ArchUnit will provide detailed error messages indicating:

1. **What rule was violated**
2. **Which classes are involved**
3. **Why the violation occurred**
4. **How to fix the violation**

## Example Violations

### Domain Layer Violation

```
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'Domain layer should not depend on other layers' was violated (1 times):
Class <io.unbyte.sandbox.domain.model.SomeEntity> depends on class <io.unbyte.sandbox.application.service.SomeService>
```

### Application Layer Violation

```
java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'Application layer should not depend on infrastructure' was violated (1 times):
Class <io.unbyte.sandbox.application.service.SomeService> depends on class <io.unbyte.sandbox.infrastructure.persistence.SomeRepository>
```

## Benefits

1. **Enforced Architecture**: Rules are automatically enforced during development
2. **Early Detection**: Violations are caught early in the development process
3. **Documentation**: Tests serve as living documentation of architectural principles
4. **Refactoring Safety**: Changes that violate architecture are immediately detected
5. **Team Consistency**: All team members follow the same architectural rules

## Maintenance

- **Adding New Rules**: Add new rules to the appropriate rule class
- **Modifying Rules**: Update existing rules as architecture evolves
- **Removing Rules**: Remove rules that are no longer applicable
- **Documentation**: Update this document when rules change

## Best Practices

1. **Run Tests Frequently**: Run architecture tests as part of the CI/CD pipeline
2. **Fix Violations Immediately**: Address violations as soon as they are detected
3. **Review Violations**: Understand why violations occur before fixing them
4. **Update Rules**: Keep rules up-to-date with architectural changes
5. **Team Education**: Ensure all team members understand the architectural rules
