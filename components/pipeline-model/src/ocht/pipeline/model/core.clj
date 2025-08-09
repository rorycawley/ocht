(ns ocht.pipeline.model.core
  "Core implementation of pipeline model parsing and validation.")

(defn parse-step
  "Parse a pull or push step."
  [step]
  (let [{:keys [connector config]} step]
    {:connector (keyword connector)
     :config (or config {})}))

(defn parse-transform-step
  "Parse a single transform step."
  [step]
  (let [{:keys [fn args]} step]
    {:fn (keyword fn)
     :args (or args {})}))

(defn parse-pipeline
  "Parse EDN pipeline definition into internal model.
  
  Expected EDN structure:
  {:id \"pipeline-name\"
   :pull {:connector :csv :config {:file \"data.csv\"}}
   :transform [{:fn :filter :args {:predicate #(> (:amount %) 100)}}
               {:fn :map :args {:f #(assoc % :category \"high-value\")}}]
   :push {:connector :console :config {}}}"
  [pipeline-edn]
  (let [{:keys [id pull transform push]} pipeline-edn]
    {:id id
     :pull (parse-step pull)
     :transform (mapv parse-transform-step transform)
     :push (parse-step push)}))

(defn validate-pipeline
  "Validate parsed pipeline model."
  [pipeline]
  (let [errors (cond-> []
                 (not (:id pipeline))
                 (conj {:error :missing-id :message "Pipeline must have an id"})
                 
                 (not (:pull pipeline))
                 (conj {:error :missing-pull :message "Pipeline must have a pull step"})
                 
                 (not (:push pipeline))
                 (conj {:error :missing-push :message "Pipeline must have a push step"})
                 
                 (not (sequential? (:transform pipeline)))
                 (conj {:error :invalid-transform :message "Transform must be a sequence"}))]
    {:valid? (empty? errors)
     :errors errors}))