# Java Records Migration Guide

## Overview

This document outlines opportunities to leverage Java Records in the sandbox application, replacing traditional classes where appropriate.

## What are Java Records?

Java Records (introduced in Java 14, stable in Java 16) are a special kind of class designed to be transparent carriers for immutable data. They provide:

- **Automatic implementations** of `equals()`, `hashCode()`, and `toString()`
- **Compact syntax** for data classes
- **Immutability** by default
- **Validation** through compact constructors
- **Jackson compatibility** for JSON serialization

## Excellent Candidates for Records

### ✅ **Perfect for Records**

These classes are ideal candidates because they are:

- Simple data carriers
- Immutable by design
- Have no complex inheritance
- Used primarily for data transfer

#### 1. Application Layer DTOs

- `CreateCommand` → `CreateCommandRecord`
- `GetByIdQuery` → `GetByIdQueryRecord`

#### 2. Web Layer DTOs

- `HelloResponse` → `HelloResponseRecord`
- `HealthResponse` → `HealthResponseRecord`
- `CreateRequest` → `CreateRequestRecord`
- `HelloRequest.RequestItem` → `RequestItemRecord`

### ❌ **Not Suitable for Records**

These classes should remain as traditional classes:

#### 1. Abstract Base Classes

- `BaseDto` - Abstract class with inheritance
- `WebDto` - Abstract class with inheritance

#### 2. Complex Classes with Mutable State

- `ErrorResponse` - Complex nested structure with mutable state
- `HelloRequest` - Has validation annotations and mutable state
- `ApplicationException` - Exception class with complex builder pattern

## Benefits of Using Records

### 1. **Reduced Boilerplate**

```java
// Traditional class (34 lines)
public class CreateCommand {
    private String name;
    private String description;

    public CreateCommand() {}
    public CreateCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

// Record equivalent (3 lines + validation)
public record CreateCommandRecord(String name, String description) {
    public CreateCommandRecord {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
```

### 2. **Automatic Implementations**

Records automatically provide:

- `equals()` and `hashCode()` based on all components
- `toString()` with all component values
- Accessor methods (getters) for all components

### 3. **Immutability by Default**

Records are immutable by design, preventing accidental state changes.

### 4. **Jackson Compatibility**

Records work seamlessly with Jackson for JSON serialization/deserialization.

## Migration Strategy

### Phase 1: Create Record Versions

1. Create new record classes alongside existing classes
2. Test that records work with existing Jackson configuration
3. Verify validation annotations work correctly

### Phase 2: Update Controllers

1. Update controller methods to use record types
2. Update service layer to accept record types
3. Test all endpoints to ensure compatibility

### Phase 3: Remove Old Classes

1. Remove traditional class versions
2. Update any remaining references
3. Run full test suite

## Example Migrations

### CreateCommand → CreateCommandRecord

```java
// Before
public class CreateCommand {
    private String name;
    private String description;
    // ... 30+ lines of boilerplate
}

// After
public record CreateCommandRecord(String name, String description) {
    public CreateCommandRecord {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
```

### HelloResponse → HelloResponseRecord

```java
// Before
public class HelloResponse {
    @JsonProperty("message") private String message;
    @JsonProperty("timestamp") private String timestamp;
    @JsonProperty("service") private String service;
    // ... 40+ lines of boilerplate
}

// After
public record HelloResponseRecord(
    @JsonProperty("message") String message,
    @JsonProperty("timestamp") String timestamp,
    @JsonProperty("service") String service
) {
    public HelloResponseRecord {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        // ... other validations
    }
}
```

## Validation with Records

Records support validation annotations on components:

```java
public record CreateRequestRecord(
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    String name,

    String description
) {
    public CreateRequestRecord {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
```

## Testing Records

Records work seamlessly with existing testing frameworks:

```java
@Test
void testCreateCommandRecord() {
    CreateCommandRecord command = new CreateCommandRecord("Test", "Description");

    assertThat(command.name()).isEqualTo("Test");
    assertThat(command.description()).isEqualTo("Description");
    assertThat(command.toString()).contains("Test");
    assertThat(command).isEqualTo(new CreateCommandRecord("Test", "Description"));
}
```

## Performance Benefits

Records provide several performance benefits:

- **Reduced memory footprint** (no extra object headers)
- **Faster equals/hashCode** (optimized implementations)
- **Better JVM optimizations** (immutable objects)

## Conclusion

Java Records are an excellent choice for DTOs in this application. They provide:

- **90% less boilerplate code**
- **Automatic immutability**
- **Better performance**
- **Cleaner, more readable code**
- **Full compatibility with existing frameworks**

The migration should be done incrementally, starting with the simplest DTOs and gradually moving to more complex ones.
