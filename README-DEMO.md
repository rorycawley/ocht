# Ocht Q4 2025 MVP Demo

**CSV → Transform → Console** - The foundational demo showing Ocht's core value proposition.

## Quick Demo

```bash
# Run the basic demo pipeline (no transforms, shows raw data)
clj -M -m ocht.cli.core --pipeline demo-simple.edn --verbose

# View the demo data
cat demo-data.csv

# View the pipeline definition
cat demo-simple.edn
```

## What This Demonstrates

1. **Pipelines as Data** - `demo-simple.edn` is a declarative EDN pipeline definition
2. **Pull → Transform → Push** - CSV input, optional transforms, console output
3. **Polylith Architecture** - Composable components with clean separation
4. **REPL-Driven Development** - All components tested interactively
5. **Functional Core** - Pure transformations, effects at edges only

## Demo Pipeline Flow

1. **Pull**: Read `demo-data.csv` (6 sales/refund records)
2. **Transform**: No transforms in basic demo (empty array `[]`)
3. **Push**: Display as formatted table

## Expected Output

```
Executing pipeline: simple-demo
id	customer	amount	type	date
1	Acme Corp	1250.50	sale	2024-01-15
2	Tech Solutions	75.00	refund	2024-01-16
3	Global Industries	2400.75	sale	2024-01-17
4	StartupXYZ	450.25	sale	2024-01-18
5	Enterprise LLC	25.00	refund	2024-01-19
6	Innovation Co	3200.00	sale	2024-01-20
✓ Pipeline executed successfully
Pipeline ID: simple-demo
Result count: 6
```

## Components Built

- **pipeline-model** - Parse and validate EDN pipeline definitions
- **pipeline-transform** - Pure transformation functions (filter, map, take, group-by)
- **csv-connector** - Read CSV files with header support
- **console-connector** - Output to console (table, EDN, pretty-print formats)
- **executor** - Orchestrate Pull → Transform → Push flow
- **cli** - Command line interface

## Available Transforms

The transform system supports:
- `:filter` - Filter records with predicate functions
- `:map` - Transform records with mapping functions  
- `:take` - Limit number of records
- `:group-by` - Group records by key function

*Note: Function literals like `#()` cannot be serialized in EDN, so complex transforms would need to be pre-registered or use a different approach.*

## Architecture Highlights

- **Polylith workspace** - Scalable, testable component architecture
- **Connector protocol** - Standardized pull/push/validate interface
- **Error handling** - Structured error responses with validation
- **Streaming-ready** - Transducer-based transforms for efficiency

## Testing

All components have comprehensive test coverage:
```bash
# Run all tests from development project
clj -M:test -e "(clojure.test/run-all-tests)"
```

## Next Steps for Full Platform

- **More connectors** - JDBC, Kafka, S3, HTTP APIs
- **Advanced transforms** - ML model inference, aggregations, joins
- **Streaming execution** - Real-time data processing (same pipeline logic)
- **Function registry** - Pre-registered transform functions for EDN compatibility
- **Web UI** - Visual pipeline builder and monitoring
- **Enterprise features** - Governance, security, lineage tracking