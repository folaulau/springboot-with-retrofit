# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=TitanApiTests

# Build without tests
./mvnw clean install -DskipTests

# Create executable JAR
./mvnw clean package
```

## Architecture Overview

This is a Spring Boot 3.3.2 application that integrates with external APIs using Retrofit 2. The project follows a layered architecture:

### Key Components

1. **REST API Layer** (`/src/main/java/com/folauetau/retrofit/rest/`)
   - Retrofit-based HTTP client interfaces
   - `TitanAssetRestApi`: Connects to `https://contentapi.churchofjesuschrist.org/api/v2/asset/`
   - `TitanCollectionRestApi`: Connects to `https://titanapi.churchofjesuschrist.org/assetsearch/api/v2/collection/`

2. **Service Layer** (`/src/main/java/com/folauetau/retrofit/service/`)
   - `TitanAssetService`, `TitanCollectionService`: Retrofit service interfaces
   - `RetrofitApiClient`: Core client interface with abstract and concrete implementations
   - Client pattern with customizable OkHttp configurations

3. **DTO Layer** (`/src/main/java/com/folauetau/retrofit/dto/`)
   - `TitanAsset`: Complex asset model with language support and SEO path generation
   - `CollectionDetails`: Hierarchical collection model with parent/child relationships
   - Rich utility methods for data extraction and transformation

### Technical Stack
- Java 17
- Spring Boot 3.3.2
- Retrofit 2.11.0 with Gson converter
- OkHttp 4.12.0 with logging interceptor
- Lombok for reducing boilerplate
- JUnit 5 for testing

### Important Design Patterns
- Interface-based service definitions with Retrofit annotations
- Abstract client pattern for reusable HTTP client configuration
- Factory pattern for service creation via `createService()` method
- Language-aware content handling (English primary with translation support)

### Testing Approach
Tests are integration-focused, making real API calls to external services. Test data is often persisted to JSON files for analysis. Tests include duplicate detection, data validation, and recursive collection traversal.

### Configuration Notes
- Base URLs are currently hardcoded in REST API implementations
- No environment-specific configuration in application.properties
- 30-second read timeout configured for all API calls
- Gson configured with lenient parsing for JSON handling