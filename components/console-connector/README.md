# console-connector

Console output connector implementing the Connector protocol for displaying data to stdout.

## Purpose

Provides console output capabilities for data pipelines following the Pull → Transform → Push pattern.

## Public API

```clojure
(require '[ocht.connectors.console :as console])

;; Create connector instance
(console/create-connector)
```

## Configuration

```clojure
{:format :edn     ; :edn, :table, or :json (not yet supported)
 :pretty? true}   ; pretty-print output, defaults to true
```

## Output Formats

### EDN Format (default)
```clojure
;; Pretty printed (default)
({:id 1, :name "Alice", :amount 100}
 {:id 2, :name "Bob", :amount 200})

;; Compact
({:id 1 :name "Alice" :amount 100} {:id 2 :name "Bob" :amount 200})
```

### Table Format
```
id	name	amount
1	Alice	100
2	Bob	200
```

## Example Usage

```clojure
(let [connector (console/create-connector)
      config {:format :table}
      data [{:id 1 :name "Alice" :amount 100}
            {:id 2 :name "Bob" :amount 200}]]
  
  ;; Validate configuration
  (validate connector config)
  
  ;; Output to console
  (push connector config data {}))
```

## Notes

- Write-only connector (pull operations not supported)
- Table format works best with consistent map structures
- Pretty printing enabled by default for readability