# SSAS Driver for Metabase

A Clojure library that provides a custom driver for connecting Metabase to Microsoft SQL Server Analysis Services (SSAS) via OLAP4J.

## Overview

This driver enables Metabase to connect to and query SSAS cubes, providing access to multidimensional data through Metabase's web interface.

## Features

- Connection to SSAS databases via OLAP4J
- Automatic discovery of cubes, dimensions, hierarchies, and levels
- Metadata extraction from SSAS schema information
- Integration with Metabase's driver architecture

## Building the Driver

### Prerequisites

- Java 11 or higher
- Leiningen (for building)

### Build Steps

1. **Install Leiningen**:
   ```bash
   curl -o lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
   chmod +x lein
   export PATH=$PATH:$(pwd)
   ```

2. **Clone and build the project**:
   ```bash
   git clone <repository-url>
   cd metabase-ssas-driver
   lein deps
   lein compile
   ```

3. **Create JAR files**:
   ```bash
   # Create a basic JAR
   lein jar
   
   # Create an uberjar with all dependencies
   lein uberjar
   ```

   The built artifacts will be in the `target/` directory:
   - `target/metabase-ssas-0.1.0-SNAPSHOT.jar` - Basic JAR
   - `target/uberjar/ssas-driver.jar` - Uberjar with dependencies

## Testing the Driver

### Basic Functionality Tests

Run the included tests to verify core functionality:

```bash
# Test basic functions (requires built uberjar)
java -cp target/uberjar/ssas-driver.jar clojure.main -e "
(require '[my.ssas.driver :as driver])
(println \"Driver registration:\" (driver/register))
(println \"Connection spec:\" (driver/connection-details->spec {:host \"localhost\" :port 1433 :database \"testdb\"}))
"
```

### Unit Tests

The project includes unit tests for core functionality:

- String conversion utilities (`to-table-name`, `to-camel-case`)
- Driver registration functionality  
- Connection specification creation
- Metadata extraction functions

### Integration with Metabase

To use this driver with Metabase:

1. **Build the driver JAR**:
   ```bash
   lein uberjar
   ```

2. **Copy to Metabase plugins directory**:
   ```bash
   cp target/uberjar/ssas-driver.jar /path/to/metabase/plugins/
   ```

3. **Restart Metabase** to load the driver

4. **Configure SSAS connection** in Metabase admin interface:
   - Host: Your SSAS server hostname
   - Port: SSAS port (typically 2383 for HTTP, 2382 for XMLA)
   - Database: SSAS database/catalog name
   - Username/Password: Authentication credentials

## Configuration

### Connection Parameters

The driver accepts the following connection parameters:

- **Host**: The hostname or IP address of the SSAS server
- **Port**: The port number (default: 2382 for XMLA)
- **Database**: The SSAS database/catalog name
- **Username**: Authentication username
- **Password**: Authentication password

### SSAS Server Requirements

- Microsoft SQL Server Analysis Services (SSAS)
- XMLA endpoint enabled (for remote connections)
- Appropriate user permissions for cube access

## Architecture

### Core Components

- `my.ssas.driver`: Main driver namespace with connection and metadata functions
- `connection-details->spec`: Converts Metabase connection details to JDBC spec
- `create-metadata`: Extracts cube, dimension, and hierarchy metadata from SSAS
- `ssas-driver`: Main driver function for establishing connections

### Dependencies

- `org.clojure/clojure`: Core Clojure runtime
- `org.olap4j/olap4j`: OLAP4J library for SSAS connectivity
- `org.clojure/java.jdbc`: JDBC utilities

## Development

### Project Structure

```
src/
  my/ssas/
    driver.clj          # Main driver implementation
test/
  my/ssas/
    driver_test.clj     # Unit tests
project.clj             # Leiningen project configuration
manifest.edn            # Metabase driver manifest
```

### Local Development

1. Start a REPL:
   ```bash
   lein repl
   ```

2. Load the driver:
   ```clojure
   (require '[my.ssas.driver :as driver])
   ```

3. Test functions interactively:
   ```clojure
   (driver/register)
   (driver/connection-details->spec {:host "localhost" :port 1433 :database "testdb"})
   ```

## Troubleshooting

### Common Issues

1. **Dependency resolution errors**: Ensure you have internet access and can reach Maven repositories
2. **SSAS connection failures**: Verify SSAS server is running and XMLA endpoint is accessible
3. **Authentication errors**: Check username/password and Windows authentication settings
4. **Metabase integration issues**: Ensure JAR is in the correct plugins directory and Metabase has been restarted

### Logs

Check Metabase logs for driver loading and connection errors:
```bash
tail -f /path/to/metabase/logs/metabase.log
```

## License

Copyright © 2023

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request
