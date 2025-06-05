# SSAS Driver Usage Examples

This file contains examples of how to use the SSAS driver for Metabase.

## Building the Driver

```bash
# Ensure Leiningen is available
export PATH=/tmp:$PATH

# Clean and build
lein clean
lein uberjar

# The driver JAR will be created at:
# target/uberjar/ssas-driver.jar
```

## Testing the Driver

```bash
# Run the comprehensive test suite
./test_driver.sh

# Or test manually
java -cp target/uberjar/ssas-driver.jar clojure.main -e "
(require '[my.ssas.driver :as driver])
(println (driver/register))
"
```

## Using with Metabase

### Installation

1. Copy the driver JAR to your Metabase plugins directory:
   ```bash
   cp target/uberjar/ssas-driver.jar /path/to/metabase/plugins/
   ```

2. Restart Metabase to load the driver

### Configuration

When adding a new database in Metabase, select "My SSAS Driver" and configure:

- **Host**: `your-ssas-server.company.com`
- **Port**: `2382` (for XMLA) or `1433` (for direct connection)
- **Database**: `YourSSASDatabase`
- **Username**: `domain\username` or `username`
- **Password**: `your-password`

### Example Connection Configurations

#### Development Environment
```
Host: localhost
Port: 2382
Database: AdventureWorksDW
Username: dev_user
Password: dev_password
```

#### Production Environment
```
Host: ssas-prod.company.com
Port: 2382
Database: SalesCube
Username: DOMAIN\analyst
Password: production_password
```

## Driver API Examples

### Creating Connection Specifications

```clojure
(require '[my.ssas.driver :as driver])

; Create a connection spec
(def connection-details
  {:host "localhost"
   :port 2382
   :database "AdventureWorksDW"
   :username "user"
   :password "pass"})

(def spec (driver/connection-details->spec connection-details))
; => {:classname "my.ssas.driver.Driver"
;     :subprotocol "my-ssas-driver"
;     :subname "//localhost:2382/AdventureWorksDW"
;     :user "user"
;     :password "pass"}
```

### Driver Registration

```clojure
; Get driver metadata
(def metadata (driver/register))
(println (:metabase.driver/name metadata))
; => "My SSAS Driver"
```

### String Utilities

```clojure
; Convert dimension names to table names
(#'driver/to-table-name "Product Category")
; => "product_category"

(#'driver/to-table-name "Sales Amount Measure")
; => "sales_amount_measure"
```

## Troubleshooting

### Common Issues

1. **Driver not appearing in Metabase**
   - Ensure JAR is in the correct plugins directory
   - Check Metabase logs for loading errors
   - Restart Metabase after adding the JAR

2. **Connection failures**
   - Verify SSAS server is accessible
   - Check firewall settings
   - Ensure XMLA endpoint is enabled on SSAS
   - Verify credentials and permissions

3. **Metadata discovery issues**
   - Check user permissions on SSAS cubes
   - Verify cube and database names are correct
   - Review SSAS logs for connection attempts

### Debug Mode

To enable debug logging, add logging configuration to your Metabase setup:

```
LOG_LEVEL=DEBUG
```

Or check specific driver logs in the Metabase admin interface.

## Development Notes

### Key Components

- `my.ssas.driver/connection-details->spec`: Converts Metabase connection config to JDBC spec
- `my.ssas.driver/register`: Returns driver metadata for Metabase registration
- `my.ssas.driver/create-metadata`: Extracts cube/dimension metadata from SSAS
- `my.ssas.driver/ssas-driver`: Main driver entry point

### Extension Points

To extend the driver functionality:

1. **Add new connection parameters**: Modify `connection-details->spec`
2. **Custom metadata extraction**: Extend `create-metadata` function
3. **Query optimization**: Add query processing functions
4. **Authentication**: Enhance connection setup for different auth methods

### Testing

Run the test suite with:
```bash
./test_driver.sh
```

Or individual tests:
```bash
java -cp target/uberjar/ssas-driver.jar clojure.main -e "
(require '[my.ssas.driver :as driver])
(println 'Testing string conversion:')
(println (#'driver/to-table-name \"Test Dimension\"))
"
```