# connector-protocol

Shared protocol definition for all data pipeline connectors.

## Purpose

Provides the standard `Connector` protocol that all data source and sink connectors must implement to participate in Ocht's Pull → Transform → Push pipeline pattern.

## Public API

```clojure
(require '[ocht.connector :as conn])

;; Protocol reference
conn/Connector

;; Utility functions
(conn/connector? obj)
(conn/validate-connector-result result operation)
```

## Connector Protocol

All connectors must implement these three methods:

### `pull`
Pull data from source. Should be idempotent and bounded.

```clojure
(pull connector config options) 
;; Returns: sequence of data records
```

### `push`  
Push data to destination. Should be idempotent when possible.

```clojure
(push connector config data options)
;; Returns: data sequence (for chaining)
```

### `validate`
Validate configuration without side effects. Must be fast.

```clojure
(validate connector config)
;; Returns: {:valid? boolean :errors [error-maps]}
```

## Implementation Example

```clojure
(defrecord MyConnector []
  conn/Connector
  (pull [_ config options]
    ;; Read data from source
    (read-data-source config))
  
  (push [_ config data options]  
    ;; Write data to destination
    (write-data-destination config data)
    data)
  
  (validate [_ config]
    ;; Validate config structure
    (if (valid-config? config)
      {:valid? true :errors []}
      {:valid? false :errors [{:error :invalid-config :message "..."}]})))
```

## Design Principles

- **Idempotent operations** when possible
- **Fast validation** without side effects  
- **Structured error reporting** for debugging
- **Chainable push operations** (return data)
- **Consistent interface** across all connectors