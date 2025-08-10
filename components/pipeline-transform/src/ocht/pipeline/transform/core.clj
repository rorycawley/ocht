(ns ocht.pipeline.transform.core
  "Core transformation implementations.")

(defn filter-transform
  "Create a filter transducer from args."
  [{:keys [predicate]}]
  (filter predicate))

(defn map-transform
  "Create a map transducer from args."
  [{:keys [f]}]
  (map f))

(defn take-transform
  "Create a take transducer from args."
  [{:keys [n]}]
  (take n))

(defn group-by-transform
  "Create a group-by transformation from args.
  Note: This is not a transducer as it needs to realize the full sequence."
  [{:keys [key-fn]}]
  (fn [data]
    (group-by key-fn data)))

(def transform-registry
  "Registry of available transform functions."
  {:filter filter-transform
   :map map-transform
   :take take-transform
   :group-by group-by-transform})

(defn create-transform
  "Create a transform function from a step definition."
  [{:keys [transform-fn args] :as step}]
  {:pre [(map? step) (keyword? transform-fn)]}
  (if-let [transform-factory (get transform-registry transform-fn)]
    (transform-factory (or args {}))
    (throw (ex-info "Unknown transform function" {:step step :available (keys transform-registry)}))))

(defn create-transducer
  "Create a transducer from a sequence of transform steps."
  [transform-steps]
  (let [transducers (for [step transform-steps
                          :let [transform (create-transform step)]
                          :when (fn? transform)]
                      transform)]
    (apply comp transducers)))

(defn apply-transforms
  "Apply transform steps to data."
  [data transform-steps]
  {:pre [(seqable? data) (sequential? transform-steps)]}
  (if (empty? transform-steps)
    data
    (let [xform (create-transducer transform-steps)]
      (sequence xform data))))