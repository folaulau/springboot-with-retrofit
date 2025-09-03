# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that uses Retrofit to interact with Church of Jesus Christ content APIs. The primary purpose is fetching, processing, and validating content collections and assets.

Projects are stored in the projects directory.

## Development Commands

### Build and Test
```bash
# Build the project
./mvnw clean install

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TitanApiTests
./mvnw test -Dtest=TitanMiniApiTests
./mvnw test -Dtest=TitanAssetApiTests

# Run the application
./mvnw spring-boot:run

# Package as JAR
./mvnw package
```

### Code Style Check
```bash
# No linting configuration found - consider adding spotless or checkstyle
```

## Architecture

### Core Components

1. **Retrofit Integration Pattern**
   - Service interfaces define API contracts (e.g., `TitanAssetService`, `TitanCollectionService`)
   - REST API implementations create Retrofit instances with custom OkHttp configurations
   - 30-second read timeout configured on all API calls

2. **Service Layer Structure**
   ```
   RetrofitApiClient (interface)
   └── AbstractRetrofitApiClient (base implementation)
       ├── TitanAssetRestApi
       └── TitanCollectionRestApi
   ```

3. **API Endpoints**
   - Base URL: `https://contentapi.churchofjesuschrist.org/api/v2/`
   - Asset endpoint: `/asset/details/id/{id}`
   - Collection endpoints handle various collection types

### Key Patterns

- **DTO Pattern**: All API responses mapped to POJOs with Lombok annotations
- **Test-Driven**: Comprehensive integration tests for all API operations
- **File Storage**: Downloads JSON data to `json_files/` directory (gitignored)

## Testing Approach

Tests focus on:
- Fetching collections and validating data integrity
- Checking for duplicate URIs and missing English collections
- Asset path validation
- Collection hierarchy validation

Run individual test methods:
```bash
./mvnw test -Dtest=TitanApiTests#testMiniCollections
```

## Important Notes

- No database configuration - this is an API client application
- No authentication/security configured for external APIs
- JSON files are stored locally in `json_files/` (excluded from git)
- Uses Gson for JSON serialization/deserialization
- Spring Boot 3.3.2 with Java 17