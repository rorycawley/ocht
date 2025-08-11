# Ocht CLI Project

Production CLI application for running Ocht data pipelines.

## Description

This project composes the CLI base with necessary components to create a standalone command-line application for executing data pipelines.

## Components Included

- **cli** (base) - Command-line interface
- **executor** - Pipeline execution engine  
- **pipeline-model** - Pipeline parsing and validation
- **pipeline-transform** - Data transformation functions
- **csv-connector** - CSV file reading
- **console-connector** - Console output
- **config** - Configuration management
- **validation** - Input validation

## Building

```bash
cd projects/ocht-cli
clojure -T:build uberjar
```

## Running

```bash
# Development
clojure -M:run --pipeline pipeline.edn

# Production JAR
java -jar target/ocht-cli.jar --pipeline pipeline.edn
```

## Testing

```bash
clojure -M:test
```