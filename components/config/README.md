# Config Component

Configuration management component following POLYLITH.md port/adapter patterns.

## Purpose

Provides centralized configuration management for Ocht applications, abstracting configuration sources behind a clean interface.

## Interface

- `load-config!` - Initialize configuration from multiple sources
- `get-config` - Retrieve configuration values by key path
- `get-env` - Get environment variables with type coercion
- `validate-config!` - Validate configuration against schema

## Configuration Sources

1. Environment variables (highest precedence) - prefix with `OCHT_`
2. Configuration files (EDN format)
3. Built-in defaults (lowest precedence)

## Example

```clojure
(require '[ocht.config.interface :as config])

;; Load configuration
(config/load-config! ["config/app.edn"])

;; Get values
(config/get-config [:ocht.executor :timeout-ms])
=> 30000

;; Environment variables
(config/get-env "PORT" 8080 :int)
=> 8080
```