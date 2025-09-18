# API Endpoints

This document describes the available REST API endpoints for the Sandbox service.

## Base URL

```
http://localhost:8080/api/v1
```

## Endpoints

### 1. Hello Endpoint

**GET** `/hello`

Returns a greeting message with sample data.

#### Response

```json
{
  "message": "Hello from Sandbox Service! 🚀",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "service": "sandbox-service"
}
```

#### Example Usage

```bash
curl -X GET http://localhost:8080/api/v1/hello
```

### 2. Health Check Endpoint

**GET** `/health`

Returns the health status of the service.

#### Response

```json
{
  "status": "UP",
  "timestamp": "2024-01-01T12:00:00.000Z",
  "service": "sandbox-service",
  "version": "1.0.0"
}
```

#### Example Usage

```bash
curl -X GET http://localhost:8080/api/v1/health
```

## Response Format

All responses are returned in JSON format with the following structure:

- **Content-Type**: `application/json`
- **Status Codes**:
  - `200 OK` - Successful request
  - `500 Internal Server Error` - Server error

## Testing

You can test the endpoints using:

1. **curl** (command line)
2. **Postman** (GUI)
3. **Browser** (for GET requests)
4. **Integration tests** (included in the project)

## Running the Application

1. Start the application:

   ```bash
   mvn spring-boot:run
   ```

2. The application will be available at: `http://localhost:8080`

3. Test the endpoints:

   ```bash
   # Hello endpoint
   curl http://localhost:8080/api/v1/hello

   # Health endpoint
   curl http://localhost:8080/api/v1/health
   ```
