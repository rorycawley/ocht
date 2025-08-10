(ns ocht.connector.protocol
  "Core connector protocol definition and validation utilities.")

(defprotocol Connector
  "Standard interface for data pipeline connectors.
  
  All connectors must implement this protocol to participate in the
  Pull → Transform → Push pipeline pattern. Operations should be:
  - pull: Idempotent and bounded (or provide streaming control)
  - push: Idempotent when possible, documented otherwise  
  - validate: Fast, side-effect free configuration validation"
  
  (pull [this config options] 
    "Pull data from the connector's source.
    
    Args:
      this - The connector instance
      config - Connector-specific configuration map
      options - Pipeline execution options
      
    Returns:
      Sequence of data records or throws exception on error")
  
  (push [this config data options] 
    "Push data to the connector's destination.
    
    Args:
      this - The connector instance  
      config - Connector-specific configuration map
      data - Sequence of data records to write
      options - Pipeline execution options
      
    Returns:
      Data sequence (for chaining) or throws exception on error")
  
  (validate [this config] 
    "Validate connector configuration without side effects.
    
    Args:
      this - The connector instance
      config - Connector-specific configuration map
      
    Returns:
      {:valid? boolean :errors [error-maps]} validation result"))

(defn connector?
  "Test whether an object implements the Connector protocol."
  [x]
  (satisfies? Connector x))

(defn validate-connector-result
  "Validate the structure of a connector operation result.
  
  Ensures validation results follow the expected format."
  [result operation]
  {:pre [(map? result)]}
  (let [required-keys #{:valid? :errors}
        has-required? (every? #(contains? result %) required-keys)
        valid-structure? (and (boolean? (:valid? result))
                              (sequential? (:errors result)))]
    (when-not (and has-required? valid-structure?)
      (throw (ex-info "Invalid connector validation result format"
                     {:result result
                      :operation operation
                      :required-keys required-keys})))
    result))