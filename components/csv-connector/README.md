# csv-connector

CSV file connector implementing the Connector protocol for reading CSV files.

## Purpose

Provides CSV file reading capabilities for data pipelines following the Pull → Transform → Push pattern.

## Public API

```clojure
(require '[ocht.connectors.csv :as csv])

;; Create connector instance
(csv/create-connector)
```

## Configuration

```clojure
{:file "path/to/data.csv"
 :headers? true}  ; optional, defaults to true
```

## Connector Protocol

```clojure
(let [connector (csv/create-connector)
      config {:file "data.csv" :headers? true}]
  
  ;; Validate configuration
  (validate connector config)
  
  ;; Pull data (returns sequence of maps)
  (pull connector config {}))
```

## Data Format

With headers:
```clojure
;; CSV: id,name,amount
;;      1,Alice,100
;;      2,Bob,200
;; Returns: ({:id "1" :name "Alice" :amount "100"}
;;           {:id "2" :name "Bob" :amount "200"})
```

Without headers:
```clojure
;; Returns: ({:col0 "id" :col1 "name" :col2 "amount"}
;;           {:col0 "1" :col1 "Alice" :col2 "100"})
```

## Notes

- Read-only connector (push operations not supported in MVP)
- Automatically handles CSV parsing and conversion to maps
- Validates file existence during configuration validation