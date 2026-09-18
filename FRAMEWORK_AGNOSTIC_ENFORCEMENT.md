# Framework-Agnostic Architecture Enforcement

## 🎯 **Overview**

This document outlines the comprehensive ArchUnit rules that enforce framework-agnostic principles in our hexagonal architecture. These rules prevent framework dependencies from leaking into the application layer, ensuring clean architecture compliance.

## 🚨 **Architecture Rules Added**

### **1. Reactive Types Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnReactiveTypes = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "reactor.core..",
                "reactor.util..",
                "org.reactivestreams..");
```

**Purpose**: Prevents `Mono`, `Flux`, and other reactive types in application layer.

### **2. Spring WebFlux Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnWebFlux = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web.reactive..",
                "org.springframework.web.reactive.function..");
```

**Purpose**: Prevents Spring WebFlux dependencies in application layer.

### **3. Spring Annotations Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotHaveSpringAnnotations = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
        .orShould().beAnnotatedWith(org.springframework.stereotype.Service.class)
        .orShould().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
        .orShould().beAnnotatedWith(org.springframework.context.annotation.Configuration.class);
```

**Purpose**: Prevents Spring annotations in application layer.

### **4. Jackson Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnJackson = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "com.fasterxml.jackson..");
```

**Purpose**: Prevents JSON serialization dependencies in application layer.

### **5. Validation Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnValidation = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.validation..",
                "javax.validation..");
```

**Purpose**: Prevents validation annotations in application layer.

### **6. Persistence Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnPersistence = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..",
                "javax.persistence..",
                "org.hibernate..");
```

**Purpose**: Prevents JPA/Hibernate dependencies in application layer.

### **7. HTTP Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnHttp = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web..",
                "org.springframework.http..",
                "jakarta.servlet..",
                "javax.servlet..");
```

**Purpose**: Prevents HTTP/web dependencies in application layer.

### **8. Micrometer Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnMicrometer = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "io.micrometer..");
```

**Purpose**: Prevents metrics dependencies in application layer.

### **9. MapStruct Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnMapStruct = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "org.mapstruct..");
```

**Purpose**: Prevents mapping framework dependencies in application layer.

### **10. OpenAPI / Swagger Prevention**

```java
@ArchTest
static final ArchRule applicationShouldNotDependOnOpenApi = ArchRuleDefinition.noClasses()
        .that().resideInAPackage(APPLICATION)
        .should().dependOnClassesThat().resideInAnyPackage(
                "io.swagger.v3..",
                "org.springdoc..");
```

**Purpose**: Keeps springdoc-openapi and Swagger annotations in the web layer.

## 🏗️ **Architecture Layers**

### **✅ Application Layer (Framework-Agnostic)**

- **Use Cases**: Pure business logic
- **Commands/Queries**: Framework-agnostic data structures
- **Ports**: Interface definitions
- **Exceptions**: Business exceptions

### **✅ Infrastructure Layer (Framework-Specific)**

- **Controllers**: Handle HTTP requests
- **Adapters**: Implement ports using frameworks
- **Configuration**: Dependency injection setup
- **Mappers**: Framework-specific data transformation

### **✅ Domain Layer (Pure)**

- **Entities**: Business objects
- **Value Objects**: Immutable data
- **Domain Services**: Business rules

## 📋 **What These Rules Prevent**

### **❌ Before (Violations)**

```java
// ❌ WRONG - Application layer with framework dependencies
@Component
public class ProcessHelloUseCase {
    @Autowired
    private ExternalApiPort port;

    public Mono<HelloResponse> processHello(HelloRequest request) {
        return port.fetchData(request.getId())
            .map(this::transformToResponse);
    }
}
```

### **✅ After (Compliant)**

```java
// ✅ CORRECT - Framework-agnostic application layer
public class ProcessHelloUseCase {
    private final ExternalApiPort port;

    public ProcessHelloUseCase(ExternalApiPort port) {
        this.port = port;
    }

    public HelloResponse processHello(ProcessHelloCommand command) {
        List<Data> data = port.fetchData(command.getId());
        return transformToResponse(data);
    }
}
```

## 🔧 **Configuration Layer**

### **Infrastructure Configuration**

```java
@Configuration
public class ApplicationConfig {

    @Bean
    public ProcessHelloUseCase processHelloUseCase(ExternalApiPort port) {
        return new ProcessHelloUseCase(port);
    }

    @Bean
    public ExternalApiPort externalApiPort() {
        return new ExternalApiAdapter();
    }
}
```

## 🎯 **Benefits**

### **1. Testability**

- **Unit tests** without framework dependencies
- **Mocking** is straightforward
- **Business logic** is isolated

### **2. Maintainability**

- **Framework changes** don't affect business logic
- **Clear boundaries** between layers
- **Easy to understand** and modify

### **3. Flexibility**

- **Swap frameworks** without changing business logic
- **Multiple implementations** of the same port
- **Technology-agnostic** business rules

### **4. Clean Architecture**

- **Dependency inversion** principle
- **Separation of concerns**
- **Framework independence**

## 🚀 **Running the Rules**

### **Test Architecture Rules**

```bash
mvn test -Dtest=HexagonalArchitectureTest
```

### **All Tests**

```bash
mvn test
```

## 📊 **Rule Coverage**

| Framework          | Prevention Rule | Status   |
| ------------------ | --------------- | -------- |
| Reactive Types     | ✅              | Enforced |
| Spring WebFlux     | ✅              | Enforced |
| Spring Annotations | ✅              | Enforced |
| Jackson            | ✅              | Enforced |
| Validation         | ✅              | Enforced |
| Persistence        | ✅              | Enforced |
| HTTP               | ✅              | Enforced |
| Micrometer         | ✅              | Enforced |
| MapStruct          | ✅              | Enforced |

## 🎉 **Result**

- ✅ **Application layer is completely framework-agnostic**
- ✅ **Business logic is pure and testable**
- ✅ **Infrastructure handles all framework concerns**
- ✅ **Clean architecture principles enforced**
- ✅ **Future violations automatically detected**

These rules ensure that your application layer remains clean, testable, and framework-agnostic, following clean architecture principles!
