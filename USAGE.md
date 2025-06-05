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

## Query Types and Request Examples

This section outlines the types of requests and queries that the SSAS driver can process when connected to Metabase.

### Supported Data Operations

The SSAS driver provides access to multidimensional data through the following types of operations:

#### 1. Metadata Discovery

The driver automatically discovers and exposes SSAS cube structure:

```sql
-- Available cubes (shown as tables in Metabase)
SELECT * FROM INFORMATION_SCHEMA.TABLES
-- Returns: Sales, Inventory, Budget, etc.

-- Cube dimensions (shown as columns)
SHOW COLUMNS FROM Sales
-- Returns: time_dimension, product_dimension, geography_dimension, etc.
```

#### 2. Dimension Browsing

Access dimensional data for filtering and grouping:

```sql
-- Browse time dimension levels
SELECT DISTINCT year FROM time_dimension
-- Returns: 2020, 2021, 2022, 2023

-- Browse product categories
SELECT DISTINCT category, subcategory FROM product_dimension
-- Returns: Electronics/Computers, Clothing/Shirts, etc.

-- Geographic dimension hierarchy
SELECT DISTINCT country, state_province, city FROM geography_dimension
-- Returns: USA/California/San Francisco, Canada/Ontario/Toronto, etc.
```

#### 3. Measure Queries

Access calculated measures and key performance indicators:

```sql
-- Basic measure access
SELECT 
    SUM(sales_amount) as total_sales,
    SUM(quantity_sold) as total_quantity
FROM sales_fact

-- Measures with dimensional filtering
SELECT 
    product_category,
    SUM(sales_amount) as category_sales
FROM sales_fact 
WHERE year = 2023
GROUP BY product_category
```

#### 4. Cross-Dimensional Analysis

Perform multi-dimensional analysis across cube dimensions:

```sql
-- Sales by product and time
SELECT 
    year,
    quarter,
    product_category,
    SUM(sales_amount) as sales,
    SUM(profit_margin) as profit
FROM sales_fact
WHERE year BETWEEN 2022 AND 2023
GROUP BY year, quarter, product_category
ORDER BY year, quarter, sales DESC

-- Geographic sales analysis
SELECT 
    country,
    state_province,
    SUM(sales_amount) as regional_sales,
    COUNT(DISTINCT customer_id) as customer_count
FROM sales_fact
GROUP BY country, state_province
HAVING SUM(sales_amount) > 100000
```

#### 5. Time-Based Analysis

Leverage time intelligence for period comparisons:

```sql
-- Monthly sales trends
SELECT 
    year,
    month,
    SUM(sales_amount) as monthly_sales
FROM sales_fact
GROUP BY year, month
ORDER BY year, month

-- Year-over-year comparison
SELECT 
    product_category,
    SUM(CASE WHEN year = 2023 THEN sales_amount END) as sales_2023,
    SUM(CASE WHEN year = 2022 THEN sales_amount END) as sales_2022
FROM sales_fact
WHERE year IN (2022, 2023)
GROUP BY product_category
```

### MDX Query Support

The driver translates SQL queries to MDX (Multidimensional Expressions) for SSAS execution:

#### Example MDX Translations

**SQL Input:**
```sql
SELECT 
    product_category,
    SUM(sales_amount)
FROM sales_cube
WHERE year = 2023
GROUP BY product_category
```

**Equivalent MDX (executed by driver):**
```mdx
SELECT 
    [Product].[Category].MEMBERS ON ROWS,
    [Measures].[Sales Amount] ON COLUMNS
FROM [Sales Cube]
WHERE [Time].[Year].[2023]
```

### Typical Use Cases

#### Business Intelligence Dashboards

```sql
-- Executive dashboard summary
SELECT 
    'Total Sales' as metric,
    SUM(sales_amount) as value
FROM sales_fact
WHERE year = 2023

UNION ALL

SELECT 
    'Total Customers' as metric,
    COUNT(DISTINCT customer_id) as value
FROM sales_fact
WHERE year = 2023
```

#### Sales Performance Analysis

```sql
-- Top performing products
SELECT 
    product_name,
    SUM(sales_amount) as total_sales,
    SUM(quantity_sold) as units_sold,
    AVG(unit_price) as avg_price
FROM sales_fact
WHERE year = 2023
GROUP BY product_name
ORDER BY total_sales DESC
LIMIT 10
```

#### Customer Segmentation

```sql
-- Customer analysis by region
SELECT 
    customer_segment,
    geography_region,
    COUNT(DISTINCT customer_id) as customer_count,
    SUM(sales_amount) as segment_sales,
    AVG(order_value) as avg_order_value
FROM sales_fact
GROUP BY customer_segment, geography_region
```

#### Financial Reporting

```sql
-- Budget vs actual analysis
SELECT 
    department,
    month,
    SUM(budget_amount) as budgeted,
    SUM(actual_amount) as actual,
    SUM(actual_amount) - SUM(budget_amount) as variance
FROM budget_fact
WHERE year = 2023
GROUP BY department, month
ORDER BY department, month
```

### Advanced Query Patterns

#### Calculated Fields

```sql
-- Profit margin calculation
SELECT 
    product_category,
    SUM(sales_amount) as revenue,
    SUM(cost_amount) as costs,
    (SUM(sales_amount) - SUM(cost_amount)) / SUM(sales_amount) * 100 as profit_margin_percent
FROM sales_fact
GROUP BY product_category
```

#### Moving Averages

```sql
-- 3-month moving average
SELECT 
    year,
    month,
    SUM(sales_amount) as monthly_sales,
    AVG(SUM(sales_amount)) OVER (
        ORDER BY year, month 
        ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
    ) as three_month_avg
FROM sales_fact
GROUP BY year, month
ORDER BY year, month
```

### Performance Considerations

#### Efficient Query Patterns

1. **Use specific dimension filters** - avoid broad queries without WHERE clauses
2. **Leverage cube aggregations** - use pre-calculated measures when available
3. **Limit result sets** - use LIMIT clauses for large datasets
4. **Group by hierarchical levels** - utilize natural cube hierarchies

#### Example Optimized Query

```sql
-- Optimized: Uses cube aggregations and specific filters
SELECT 
    product_category,
    quarter,
    sales_amount_aggregated  -- Pre-calculated measure
FROM sales_summary_cube
WHERE year = 2023 
    AND country = 'USA'
    AND product_category IN ('Electronics', 'Clothing')
```

### Data Types and Formats

The driver handles various SSAS data types:

- **Measures**: Numeric values (integers, decimals, currency)
- **Dimensions**: Text/string values for categorization
- **Time**: Date and datetime values with built-in time intelligence
- **Calculated Members**: Custom calculations defined in the cube
- **KPIs**: Key performance indicators with status and trend

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