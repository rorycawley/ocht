(ns ocht.validation.core
  "Core validation implementation."
  (:require [clojure.java.io :as io]))

(defn- error
  "Create validation error map."
  [type message & {:keys [path value]}]
  {:type type
   :message message
   :path (or path [])
   :value value})

(defn success
  "Create successful validation result."
  []
  {:valid? true :errors []})

(defn failure
  "Create failed validation result."
  [errors]
  {:valid? false :errors (if (sequential? errors) errors [errors])})

(defn validation-result
  "Create validation result from errors collection."
  [errors]
  {:valid? (empty? errors)
   :errors errors})

(defn validate-required-fields
  "Validate required fields are present."
  [data required-fields]
  (let [missing-fields (remove #(contains? data %) required-fields)]
    (if (empty? missing-fields)
      (success)
      (failure
       (map #(error :missing-field
                    (str "Required field " % " is missing")
                    :path [%])
            missing-fields)))))

(defn validate-file-path
  "Validate file path."
  [file-path]
  (cond
    (not (string? file-path))
    (failure (error :invalid-type
                    "File path must be a string"
                    :value file-path))
    
    (empty? file-path)
    (failure (error :empty-path
                    "File path cannot be empty"
                    :value file-path))
    
    (not (.exists (io/file file-path)))
    (failure (error :file-not-found
                    (str "File not found: " file-path)
                    :value file-path))
    
    (not (.canRead (io/file file-path)))
    (failure (error :file-not-readable
                    (str "File not readable: " file-path)
                    :value file-path))
    
    :else (success)))

(defn validate-connector-config
  "Validate connector configuration."
  [connector-type config]
  (case connector-type
    :csv
    (let [file-validation (when-let [file (:file config)]
                           (validate-file-path file))]
      (cond
        (not (:file config))
        (failure (error :missing-config
                        "CSV connector requires :file configuration"))
        
        (not (:valid? file-validation))
        file-validation
        
        :else (success)))
    
    :console
    (if (and (:format config)
             (not (#{:table :json :edn :raw} (:format config))))
      (failure (error :invalid-format
                      "Console format must be :table, :json, :edn, or :raw"
                      :value (:format config)))
      (success))
    
    (failure (error :unknown-connector
                    (str "Unknown connector type: " connector-type)
                    :value connector-type))))

(defn validate-transform-step
  "Validate transform step."
  [transform-step]
  (let [required-validation (validate-required-fields transform-step [:transform-fn :args])
        transform-fn (:transform-fn transform-step)]
    (cond
      (not (:valid? required-validation))
      required-validation
      
      (not (#{:filter :map :take :group-by} transform-fn))
      (failure (error :unknown-transform
                      (str "Unknown transform function: " transform-fn)
                      :value transform-fn))
      
      :else (success))))

(defn validate-pipeline-structure
  "Validate pipeline structure."
  [pipeline]
  (let [required-validation (validate-required-fields pipeline [:id :pull :push])
        pull-validation (when (:pull pipeline)
                         (validate-required-fields (:pull pipeline) [:connector :config]))
        push-validation (when (:push pipeline)
                         (validate-required-fields (:push pipeline) [:connector :config]))]
    (cond
      (not (:valid? required-validation))
      required-validation
      
      (not (:valid? pull-validation))
      pull-validation
      
      (not (:valid? push-validation))
      push-validation
      
      :else (success))))