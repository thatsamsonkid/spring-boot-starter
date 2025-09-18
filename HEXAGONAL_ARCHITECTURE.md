# Hexagonal Architecture Structure

This project follows the Hexagonal Architecture (Ports and Adapters) pattern to ensure clean separation of concerns and testability.

## Project Structure

```
src/main/java/io/unbyte/sandbox/
├── domain/                    # Domain Layer (Business Logic)
│   ├── model/                # Domain entities and value objects
│   ├── service/              # Domain services
│   ├── repository/           # Repository interfaces (ports)
│   └── exception/            # Domain-specific exceptions
├── application/              # Application Layer (Use Cases)
│   ├── port/                 # Port interfaces (contracts)
│   ├── service/              # Application services
│   ├── usecase/              # Use case implementations
│   └── dto/                  # Application DTOs (commands, queries, business DTOs)
├── infrastructure/           # Infrastructure Layer (Adapters)
│   ├── adapter/              # Adapter implementations
│   ├── config/               # Configuration classes
│   ├── persistence/          # Database adapters
│   ├── external/             # External service adapters
│   └── web/                  # Web layer DTOs (REST API, messaging)
└── shared/                   # Shared Layer (Common)
    ├── util/                 # Utility classes
    ├── dto/                  # Shared DTOs
    ├── exception/            # Shared exceptions
    └── constant/             # Application constants
```

## Layer Responsibilities

### Domain Layer

- Contains the core business logic
- Defines entities, value objects, and domain services
- Contains repository interfaces (ports)
- Should have no dependencies on other layers
- Contains domain-specific exceptions

### Application Layer

- Contains use cases and application services
- Defines ports (interfaces) for external dependencies
- Contains DTOs for data transfer
- Orchestrates domain objects to fulfill use cases
- Depends only on domain layer

### Infrastructure Layer

- Implements ports defined in application layer
- Contains adapters for external systems (database, APIs, etc.)
- Contains configuration classes
- Depends on application layer (through ports)
- Should not contain business logic

### Shared Layer

- Contains common utilities and constants
- Shared DTOs and exceptions
- Can be used by any layer
- Should not contain business logic

## Benefits

1. **Separation of Concerns**: Each layer has a clear responsibility
2. **Testability**: Easy to mock dependencies through ports
3. **Flexibility**: Easy to swap implementations (e.g., database, external APIs)
4. **Maintainability**: Changes in one layer don't affect others
5. **Clean Dependencies**: Dependencies point inward toward the domain

## DTO Organization

### Application Layer DTOs (`application/dto/`)

- **Purpose**: Business use cases and application services
- **Types**: Commands, Queries, Business DTOs
- **Examples**: `CreateCommand`, `GetByIdQuery`, `BaseDto`
- **Usage**: Data transfer between application services and domain

### Infrastructure Layer DTOs (`infrastructure/web/`)

- **Purpose**: External communication (HTTP, messaging, etc.)
- **Types**: Request DTOs, Response DTOs, Web DTOs
- **Examples**: `CreateRequest`, `Response`, `WebDto`
- **Usage**: Data transfer between external systems and application

### Shared Layer DTOs (`shared/dto/`)

- **Purpose**: Common data structures used across layers
- **Types**: Common DTOs, Value Objects
- **Usage**: Shared between multiple layers

## Usage Guidelines

1. **Domain Layer**: Should be pure business logic with no external dependencies
2. **Application Layer**: Should orchestrate domain objects and define contracts
3. **Infrastructure Layer**: Should implement contracts and handle external concerns
4. **Shared Layer**: Should contain only common utilities and constants

## Example Flow

1. Controller (Infrastructure) receives HTTP request
2. Controller calls Application Service
3. Application Service orchestrates Domain objects
4. Application Service uses Repository Port (interface)
5. Repository Adapter (Infrastructure) implements the port
6. Response flows back through the layers
