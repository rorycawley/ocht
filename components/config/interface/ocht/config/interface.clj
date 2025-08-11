(ns ocht.config.interface
  "Configuration management for Ocht applications.
  
  This component provides centralized configuration management following
  the port/adapter pattern described in POLYLITH.md. It abstracts
  configuration sources (environment variables, files, etc.) behind
  a clean interface.
  
  Key capabilities:
  - Environment variable configuration with defaults
  - Configuration file loading (EDN, JSON)
  - Type coercion and validation
  - Hierarchical configuration merging
  - Configuration validation at startup
  
  Configuration format:
    {:ocht.connector.csv {:default-delimiter \",\"
                          :max-file-size-mb 100}
     :ocht.connector.console {:default-format :table
                              :max-display-rows 1000}
     :ocht.executor {:timeout-ms 30000
                     :max-concurrent-pipelines 10}}
  
  Example usage:
    (get-config [:ocht.connector.csv :default-delimiter])
    => \",\"
    
    (get-config [:ocht.executor :timeout-ms] 60000)
    => 30000"
  (:require [ocht.config.core]))

(defn load-config!
  "Load configuration from environment and files.
  
  Initializes the configuration system by loading from various sources
  in order of precedence:
  1. Environment variables (highest precedence)
  2. Configuration files specified
  3. Built-in defaults (lowest precedence)
  
  Args:
    config-files - Optional vector of config file paths to load
                   
  Returns:
    Configuration map
    
  Example:
    (load-config! [\"config/app.edn\" \"config/local.edn\"])"
  ([] (ocht.config.core/load-config! []))
  ([config-files] (ocht.config.core/load-config! config-files)))

(defn get-config
  "Get configuration value by key path.
  
  Retrieves configuration values using a key path vector.
  Supports default values if the configuration key is not found.
  
  Args:
    key-path - Vector of keys to navigate configuration hierarchy
    default  - Optional default value if key not found
    
  Returns:
    Configuration value or default
    
  Example:
    (get-config [:database :host])
    (get-config [:database :port] 5432)"
  ([key-path] (ocht.config.core/get-config key-path))
  ([key-path default] (ocht.config.core/get-config key-path default)))

(defn get-env
  "Get environment variable with optional default and type coercion.
  
  Args:
    env-var - Environment variable name (string)
    default - Optional default value
    coerce  - Optional coercion function (:int, :bool, or custom fn)
    
  Returns:
    Environment variable value, coerced if specified
    
  Example:
    (get-env \"PORT\" 8080 :int)
    (get-env \"DEBUG\" false :bool)"
  ([env-var] (ocht.config.core/get-env env-var))
  ([env-var default] (ocht.config.core/get-env env-var default))
  ([env-var default coerce] (ocht.config.core/get-env env-var default coerce)))

(defn validate-config!
  "Validate loaded configuration against schema.
  
  Args:
    schema - Configuration schema map
    
  Throws:
    ExceptionInfo if configuration is invalid
    
  Example:
    (validate-config! {:ocht.executor {:timeout-ms :int
                                       :max-concurrent-pipelines :int}})"
  [schema]
  (ocht.config.core/validate-config! schema))