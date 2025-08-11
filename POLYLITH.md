# CLAUDE_POLYLITH.md — Enhanced Polylith Development Guide for Claude Code

**Audience:** Claude Code (AI pair-programmer) working in a Polylith workspace.

**Goal:** Produce clean, composable code that fits the Polylith architecture, maximizes reuse, and keeps development friction low. This guide tells you *what to create*, *where to put it*, *how to wire it*, and *what rules to never break*.

---

## 0) Canon Quick Links & Mental Model

### Official Documentation References
* **Architecture Overview:** [https://polylith.gitbook.io/polylith/architecture/1.-introduction](https://polylith.gitbook.io/polylith/architecture/1.-introduction)
* **Workspace:** [https://polylith.gitbook.io/polylith/architecture/2.1.-workspace](https://polylith.gitbook.io/polylith/architecture/2.1.-workspace)
* **Base:** [https://polylith.gitbook.io/polylith/architecture/2.2.-base](https://polylith.gitbook.io/polylith/architecture/2.2.-base)
* **Component:** [https://polylith.gitbook.io/polylith/architecture/2.3.-component](https://polylith.gitbook.io/polylith/architecture/2.3.-component)
* **Development project:** [https://polylith.gitbook.io/polylith/architecture/2.4.-development](https://polylith.gitbook.io/polylith/architecture/2.4.-development)
* **Project:** [https://polylith.gitbook.io/polylith/architecture/2.6.-project](https://polylith.gitbook.io/polylith/architecture/2.6.-project)
* **Bring it all together:** [https://polylith.gitbook.io/polylith/architecture/bring-it-all-together](https://polylith.gitbook.io/polylith/architecture/bring-it-all-together)
* **Simplicity:** [https://polylith.gitbook.io/polylith/architecture/simplicity](https://polylith.gitbook.io/polylith/architecture/simplicity)
* **Testing:** [https://polylith.gitbook.io/polylith/testing/testing-overview](https://polylith.gitbook.io/polylith/testing/testing-overview)

### Core Mental Model
* **Components** hold pure business capability. **Bases** adapt that capability to the outside world. **Projects** compose one base + N components into a deployable. **Development** is a single, fast playground.
* Cross-brick calls are *always* via **interface namespaces**. Implementation namespaces are private.
* Multiple components can expose the **same interface** to allow swappable implementations; a project may include **only one** implementation per interface.
* **Think in capabilities, not layers** — organize by what the code does, not by technical concerns.

---

## 1) Vocabulary Cheat-Sheet (Use These Words Precisely)

* **Workspace** — One monorepo containing all code and projects: `bases/`, `components/`, `projects/`, `development/`, plus config (`workspace.edn`, `deps.edn`).
* **Brick** — A reusable building block: either a **component** or a **base**.
* **Component** — Where business logic lives. Has an **interface** namespace (public contract) and **implementation** namespaces (private).
* **Base** — The thin adapter layer that exposes functionality to the outside world (CLI, HTTP, lambda, etc.). Delegates to component interfaces.
* **Project** — A deployable composition of exactly one base *(usually)* + many components + libraries. Projects build artifacts.
* **Development project** — A single project for day-to-day development: one REPL/IDE that sees all bricks you're working with.
* **Interface** — The public API contract of a component, defining what it can do without revealing how.
* **Profile** — A named configuration in a project for different environments or builds.
* **Lib** — External third-party library dependency (not a brick).

---

## 2) Workspace Layout (Expected Structure)

```
workspace/
├── bases/                         # External adapters
│   └── <base-name>/
│       ├── src/
│       │   └── <ns>/
│       │       └── <base-name>/
│       │           ├── api.clj   # Public API exported to outside world
│       │           └── core.clj  # Thin orchestration delegating to components
│       ├── test/
│       └── resources/
├── components/                    # Business capabilities
│   └── <component-name>/
│       ├── src/
│       │   └── <ns>/
│       │       └── <component-name>/
│       │           ├── interface.clj # Public functions for other bricks
│       │           └── core.clj      # Private implementation
│       ├── test/
│       └── resources/
├── projects/                      # Deployable artifacts
│   └── <project-name>/
│       ├── deps.edn              # Lists included bricks + project-only libs
│       └── resources/            # Project-specific config
├── development/                   # Local development environment
│   ├── deps.edn                  # REPL/IDE config with active bricks
│   └── src/
│       └── user.clj              # REPL helpers (optional)
├── deps.edn                      # Workspace-level config, :poly alias
├── workspace.edn                 # Polylith tool configuration
├── .gitignore
└── README.md                     # Workspace overview
```

### Additional Optional Directories
```
workspace/
├── docs/                         # Workspace-wide documentation
├── scripts/                      # Build/deploy automation
└── tools/                        # Development utilities
```

---

## 3) Golden Rules (Never Break These)

1. **Business logic lives in components.** Bases stay thin and adapter-only.
2. **Depend on interfaces only.** A component may call another component's `...interface` ns; **never** import another component's `core`/impl ns.
3. **One-way dependency:** Bases may depend on components; **components must never depend on bases.**
4. **Isolate side-effects behind interfaces.** Treat interfaces as ports; provide swappable adapters as separate components.
5. **Keep component internals functional.** Data-in/data-out; keep effects at the edges.
6. **Small, stable interfaces.** Make intent explicit; avoid leaking implementation details (e.g., DB schemas).
7. **Tests at boundaries.** Component tests verify interface behavior; base tests verify API contracts.
8. **One fast development project.** Keep it lean; optimize REPL speed and feedback.
9. **Projects compose, bricks don't.** Bricks are reusable; projects decide which implementation to use per interface.
10. **No cycles.** If you need a cycle, extract a new interface to invert the dependency.
11. **Document locally.** Each brick has a README with purpose, public APIs, and examples.
12. **`poly check` must be green.** Make it a CI gate alongside `poly test`.
13. **Version interfaces carefully.** Breaking changes require new interface versions or migration paths.
14. **Prefer composition over inheritance.** Build capabilities by combining smaller components.

---

## 4) Day-0: Creating/Extending a Workspace

### Initial Setup

```bash
# Install Polylith tool
brew install polyfy/polylith/poly   # macOS
# or: bash < <(curl -s https://raw.githubusercontent.com/polyfy/polylith/master/scripts/install.sh)

# Create workspace
poly create workspace name:myws top-ns:com.mycompany

# Verify installation
cd myws
clojure -M:poly info
```

### Create Initial Bricks & Project

```bash
# Create your first component (business logic)
poly create component name:user

# Create your first base (external adapter)
poly create base name:rest-api

# Create your first project (deployable)
poly create project name:user-service

# Optional: Create with custom interface name
poly create component name:payment interface:billing
```

### Wire Dependencies

**`projects/user-service/deps.edn`:**
```clojure
{:deps {com.mycompany/user        {:local/root "../../components/user"}
        com.mycompany/rest-api    {:local/root "../../bases/rest-api"}
        org.clojure/clojure        {:mvn/version "1.11.1"}}
 :aliases {:build {...}            ; Build tooling
           :test {...}}}            ; Test runner
```

**`development/deps.edn`:**
```clojure
{:deps {com.mycompany/user        {:local/root "../components/user"}
        com.mycompany/rest-api    {:local/root "../bases/rest-api"}
        ;; Add all bricks you're actively developing
        }
 :aliases {:dev {:extra-paths ["src" "test"]
                 :extra-deps {nrepl/nrepl {:mvn/version "1.0.0"}}}}}
```

### Essential Commands

```bash
clojure -M:poly info              # Workspace overview
clojure -M:poly info :loc         # Lines of code analysis
clojure -M:poly check             # Validate deps & structure
clojure -M:poly test              # Run incremental tests
clojure -M:poly deps              # Show dependency graph
```

---

## 5) Day-to-Day Workflow (Claude's Default Flow)

### Standard Development Cycle

1. **Start development environment**
   ```bash
   cd development
   clojure -M:dev:nrepl  # or your preferred REPL startup
   ```

2. **Add/modify a component** (business capability):
   - Create/edit `components/<name>/src/<ns>/<name>/interface.clj`
   - Implement in `components/<name>/src/<ns>/<name>/core.clj`
   - Keep interface minimal; delegate to core

3. **Expose externally via base** (if needed):
   - Implement handlers in `bases/<name>/src/<ns>/<name>/api.clj`
   - Call component interfaces, never implementations

4. **Wire bricks into projects**:
   - Update `projects/<proj>/deps.edn`
   - Update `development/deps.edn` for REPL access

5. **Write/update tests**:
   - Component tests in `components/<name>/test/`
   - Base tests in `bases/<name>/test/`

6. **Validate & iterate**:
   ```bash
   clojure -M:poly check
   clojure -M:poly test
   clojure -M:poly info :changed
   ```

7. **Commit atomically**:
   ```bash
   git add .
   git commit -m "Add payment processing to user component

   - Extended user.interface with payment methods
   - Added payment validation in user.core
   - Updated rest-api base to expose /payments endpoint"
   ```

### REPL-Driven Development Tips

```clojure
;; In development/src/user.clj (optional REPL helpers)
(ns user
  (:require [clojure.tools.namespace.repl :as repl]))

(defn reset []
  (repl/refresh))

(defn run-tests []
  (clojure.test/run-all-tests #"com\.mycompany\..*-test"))
```

---

## 6) Implementing a Component (Complete Examples)

### Basic Component Structure

**`components/user/src/com/mycompany/user/interface.clj`**
```clojure
(ns com.mycompany.user.interface
  "Public API for user management capability."
  (:refer-clojure :exclude [get])
  (:require [com.mycompany.user.core :as impl]))

;; Public API - keep minimal and stable
(defn create!
  "Creates a new user with the given attributes.
  Returns the created user with generated ID."
  [attrs]
  (impl/create! attrs))

(defn get
  "Retrieves a user by ID. Returns nil if not found."
  [id]
  (impl/get id))

(defn update!
  "Updates a user with the given patch data.
  Returns the updated user."
  [id patch]
  (impl/update! id patch))

(defn list-by-email
  "Finds users by email domain."
  [domain]
  (impl/list-by-email domain))
```

**`components/user/src/com/mycompany/user/core.clj`**
```clojure
(ns com.mycompany.user.core
  (:require [com.mycompany.validation.interface :as validation]
            [com.mycompany.storage.interface :as storage]
            [com.mycompany.events.interface :as events]))

(defn- normalize-attrs [attrs]
  (-> attrs
      (update :email clojure.string/lower-case)
      (dissoc :temp-fields)))

(defn create! [attrs]
  (let [normalized (normalize-attrs attrs)
        errors (validation/validate-user normalized)]
    (if (seq errors)
      (throw (ex-info "Invalid user data" {:errors errors}))
      (let [user (assoc normalized :id (random-uuid))]
        (storage/put! :users (:id user) user)
        (events/publish! :user-created user)
        user))))

(defn get [id]
  (storage/fetch :users id))

(defn update! [id patch]
  (if-let [existing (get id)]
    (let [updated (merge existing patch)]
      (storage/put! :users id updated)
      (events/publish! :user-updated {:id id :changes patch})
      updated)
    (throw (ex-info "User not found" {:id id}))))

(defn list-by-email [domain]
  (storage/query :users {:email-domain domain}))
```

### Component with Tests

**`components/user/test/com/mycompany/user/interface_test.clj`**
```clojure
(ns com.mycompany.user.interface-test
  (:require [clojure.test :refer :all]
            [com.mycompany.user.interface :as user]
            [com.mycompany.test-fixtures.interface :as fixtures]))

(use-fixtures :each fixtures/clear-storage!)

(deftest create-user-test
  (testing "creates user with valid data"
    (let [attrs {:name "Alice" :email "alice@example.com"}
          result (user/create! attrs)]
      (is (uuid? (:id result)))
      (is (= "Alice" (:name result)))
      (is (= "alice@example.com" (:email result)))))
  
  (testing "rejects invalid email"
    (is (thrown-with-msg? Exception #"Invalid user data"
          (user/create! {:name "Bob" :email "not-an-email"})))))

(deftest update-user-test
  (testing "updates existing user"
    (let [user (user/create! {:name "Charlie" :email "charlie@example.com"})
          updated (user/update! (:id user) {:name "Charles"})]
      (is (= "Charles" (:name updated)))
      (is (= "charlie@example.com" (:email updated))))))
```

### Port & Adapter Pattern

**Port Definition** — `components/storage/src/com/mycompany/storage/interface.clj`
```clojure
(ns com.mycompany.storage.interface
  "Storage port - defines contract for persistence.
   NOTE: This is a PORT component - it defines the interface only.
   Actual implementations are in separate adapter components.")

;; Port definition - no implementation here
(defn put!
  "Stores value under collection and key."
  [collection k v])

(defn fetch
  "Retrieves value from collection by key."
  [collection k])

(defn query
  "Queries collection with criteria map."
  [collection criteria])

(defn delete!
  "Removes entry from collection."
  [collection k])
```

**In-Memory Adapter** — `components/storage-memory/src/com/mycompany/storage/interface.clj`
```clojure
(ns com.mycompany.storage.interface
  "In-memory implementation of storage port."
  (:require [com.mycompany.storage-memory.core :as impl]))

(def put! impl/put!)
(def fetch impl/fetch)
(def query impl/query)
(def delete! impl/delete!)
```

**`components/storage-memory/src/com/mycompany/storage_memory/core.clj`**
```clojure
(ns com.mycompany.storage-memory.core)

(defonce ^:private store (atom {}))

(defn put! [collection k v]
  (swap! store assoc-in [collection k] v)
  v)

(defn fetch [collection k]
  (get-in @store [collection k]))

(defn query [collection criteria]
  ;; Simplified query implementation
  (let [coll (get @store collection {})]
    (filter #(every? (fn [[k v]] (= v (get % k))) criteria)
            (vals coll))))

(defn delete! [collection k]
  (swap! store update collection dissoc k)
  nil)
```

**PostgreSQL Adapter** — `components/storage-postgres/src/com/mycompany/storage/interface.clj`
```clojure
(ns com.mycompany.storage.interface
  (:require [com.mycompany.storage-postgres.core :as impl]))

(def put! impl/put!)
(def fetch impl/fetch)
(def query impl/query)
(def delete! impl/delete!)
```

---

## 7) Implementing a Base (Complete Examples)

### HTTP REST API Base

**`bases/rest-api/src/com/mycompany/rest_api/api.clj`**
```clojure
(ns com.mycompany.rest-api.api
  "HTTP handlers for REST API endpoints."
  (:require [com.mycompany.user.interface :as user]
            [com.mycompany.auth.interface :as auth]
            [ring.util.response :as response]))

;; Request handlers - thin delegation to components
(defn handle-create-user [req]
  (try
    (let [token (get-in req [:headers "authorization"])
          _ (auth/verify-admin! token)
          attrs (:body req)
          user (user/create! attrs)]
      (-> (response/response user)
          (response/status 201)
          (response/header "Location" (str "/users/" (:id user)))))
    (catch Exception e
      (-> (response/response {:error (.getMessage e)})
          (response/status 400)))))

(defn handle-get-user [req]
  (let [id (-> req :path-params :id parse-uuid)]
    (if-let [user (user/get id)]
      (response/response user)
      (-> (response/response {:error "User not found"})
          (response/status 404)))))

(defn handle-update-user [req]
  (try
    (let [id (-> req :path-params :id parse-uuid)
          patch (:body req)
          updated (user/update! id patch)]
      (response/response updated))
    (catch Exception e
      (-> (response/response {:error (.getMessage e)})
          (response/status 400)))))
```

**`bases/rest-api/src/com/mycompany/rest_api/core.clj`**
```clojure
(ns com.mycompany.rest-api.core
  "Server setup and routing configuration."
  (:require [com.mycompany.rest-api.api :as api]
            [com.mycompany.rest-api.middleware :as middleware]
            [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]))

(def routes
  ["/api"
   ["/users"
    ["" {:post api/handle-create-user
         :get api/handle-list-users}]
    ["/:id" {:get api/handle-get-user
             :put api/handle-update-user}]]])

(def app
  (ring/ring-handler
    (ring/router routes)
    (ring/create-default-handler)
    {:middleware [middleware/wrap-json
                  middleware/wrap-cors
                  middleware/wrap-logging]}))

(defn start-server [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (println "Starting server on port" port)
    (start-server port)))
```

### CLI Base

**`bases/cli/src/com/mycompany/cli/api.clj`**
```clojure
(ns com.mycompany.cli.api
  "CLI command implementations."
  (:require [com.mycompany.user.interface :as user]
            [com.mycompany.reporting.interface :as reporting]
            [clojure.pprint :as pp]))

(defn create-user-command [{:keys [name email role]}]
  (try
    (let [user (user/create! {:name name :email email :role role})]
      (println "User created successfully:")
      (pp/pprint user))
    (catch Exception e
      (println "Error:" (.getMessage e))
      (System/exit 1))))

(defn list-users-command [{:keys [domain format]}]
  (let [users (user/list-by-email domain)]
    (case format
      "json" (println (json/write-str users))
      "csv" (println (csv/write-csv users))
      (pp/print-table [:id :name :email] users))))
```

---

## 8) Project Wiring (Deployable Artifacts)

### Development Project Configuration

**`development/deps.edn`**
```clojure
{:deps {;; Include all bricks you're actively developing
        com.mycompany/user           {:local/root "../components/user"}
        com.mycompany/auth           {:local/root "../components/auth"}
        com.mycompany/storage-memory {:local/root "../components/storage-memory"}
        com.mycompany/rest-api       {:local/root "../bases/rest-api"}
        
        ;; Dev-only dependencies
        nrepl/nrepl                  {:mvn/version "1.0.0"}
        cider/cider-nrepl            {:mvn/version "0.28.5"}}
        
 :aliases {:dev {:extra-paths ["src" "test"]
                 :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]}
           :test {:extra-deps {lambdaisland/kaocha {:mvn/version "1.87.1366"}}
                  :exec-fn kaocha.runner/exec-fn}}}
```

### Production Project Configuration

**`projects/user-service/deps.edn`**
```clojure
{:deps {;; Base (entry point)
        com.mycompany/rest-api        {:local/root "../../bases/rest-api"}
        
        ;; Components (business logic)
        com.mycompany/user            {:local/root "../../components/user"}
        com.mycompany/auth            {:local/root "../../components/auth"}
        com.mycompany/validation      {:local/root "../../components/validation"}
        com.mycompany/events-kafka    {:local/root "../../components/events-kafka"}
        com.mycompany/storage-postgres {:local/root "../../components/storage-postgres"}
        
        ;; External libraries
        org.clojure/clojure           {:mvn/version "1.11.1"}
        ring/ring-core                {:mvn/version "1.10.0"}
        metosin/reitit                {:mvn/version "0.5.18"}}
        
 :aliases {:build {:deps {io.github.clojure/tools.build {:git/tag "v0.9.6" :git/sha "8e78bcc"}}
                   :ns-default build}
           :run {:main-opts ["-m" "com.mycompany.rest-api.core"]}}}
```

---

## 9) Testing Strategy

### Component Testing

**Focus:** Interface contracts and business logic

```clojure
(ns com.mycompany.payment.interface-test
  (:require [clojure.test :refer :all]
            [com.mycompany.payment.interface :as payment]
            [com.mycompany.test-helpers.interface :as helpers]))

(deftest process-payment-test
  (helpers/with-test-adapters
    (testing "successful payment"
      (let [result (payment/process! {:amount 100 :currency "USD"})]
        (is (= :success (:status result)))
        (is (uuid? (:transaction-id result)))))
    
    (testing "insufficient funds"
      (is (thrown-with-msg? Exception #"Insufficient funds"
            (payment/process! {:amount 1000000 :currency "USD"}))))))
```

### Base Testing

**Focus:** External API contracts

```clojure
(ns com.mycompany.rest-api.api-test
  (:require [clojure.test :refer :all]
            [com.mycompany.rest-api.api :as api]
            [ring.mock.request :as mock]))

(deftest rest-api-contract-test
  (testing "POST /users returns 201 with Location header"
    (let [request (-> (mock/request :post "/users")
                     (mock/json-body {:name "Test" :email "test@example.com"}))
          response (api/handle-create-user request)]
      (is (= 201 (:status response)))
      (is (contains? (:headers response) "Location")))))
```

---

## 10) Common Patterns & Solutions

### Configuration Management

**Component:** `components/config/src/com/mycompany/config/interface.clj`
```clojure
(ns com.mycompany.config.interface
  (:require [com.mycompany.config.core :as impl]))

(defn get
  "Get config value by key or path."
  [k]
  (impl/get k))

(defn get-in
  "Get nested config value."
  [ks]
  (impl/get-in ks))
```

### Event Bus Pattern

**Port:** `components/events/src/com/mycompany/events/interface.clj`
```clojure
(ns com.mycompany.events.interface)

(defn publish!
  "Publish an event to the bus."
  [event-type payload])

(defn subscribe!
  "Subscribe to events of given type."
  [event-type handler])
```

### Validation Component

**Component:** `components/validation/src/com/mycompany/validation/interface.clj`
```clojure
(ns com.mycompany.validation.interface
  (:require [com.mycompany.validation.core :as impl]))

(defn validate-user
  "Validates user data, returns seq of errors or nil."
  [user-data]
  (impl/validate-user user-data))

(defn validate-email
  "Validates email format."
  [email]
  (impl/validate-email email))
```

---

## 11) Advanced Topics

### Profile Management

**`workspace.edn`**
```clojure
{:top-namespace "com.mycompany"
 :interface-ns "interface"
 :default-profile-name "default"
 :compact-views #{}
 :vcs {:name "git"
       :auto-add false}
 :tag-patterns {:stable "stable-*"
                :release "v[0-9]*"}
 :projects {"development" {:alias "dev"}
            "user-service" {:alias "user"}
            "admin-service" {:alias "admin"}}}
```

### Multi-Base Projects (Rare)

```clojure
;; projects/full-stack/deps.edn
{:deps {;; Multiple bases for different protocols
        com.mycompany/rest-api   {:local/root "../../bases/rest-api"}
        com.mycompany/graphql-api {:local/root "../../bases/graphql-api"}
        com.mycompany/websocket   {:local/root "../../bases/websocket"}
        ;; Shared components
        com.mycompany/user        {:local/root "../../components/user"}
        ;; ...
        }}
```

### Library Components (Shared Utils)

```clojure
;; components/lib-utils/src/com/mycompany/lib_utils/interface.clj
(ns com.mycompany.lib-utils.interface
  "Pure utility functions shared across components."
  (:require [com.mycompany.lib-utils.core :as impl]))

(def parse-uuid impl/parse-uuid)
(def format-date impl/format-date)
(def deep-merge impl/deep-merge)
```

---

## 12) CI/CD Integration

### GitHub Actions Example

```yaml
name: Polylith CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
      
      - name: Cache deps
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/deps.edn') }}
      
      - name: Install Polylith
        run: |
          curl -O https://github.com/polyfy/polylith/releases/latest/download/poly-${{ runner.os }}.jar
          sudo mv poly-*.jar /usr/local/bin/poly.jar
          echo '#!/bin/sh' | sudo tee /usr/local/bin/poly
          echo 'java -jar /usr/local/bin/poly.jar "$@"' | sudo tee -a /usr/local/bin/poly
          sudo chmod +x /usr/local/bin/poly
      
      - name: Check structure
        run: clojure -M:poly check
      
      - name: Run tests
        run: clojure -M:poly test
      
      - name: Build artifacts
        if: github.ref == 'refs/heads/main'
        run: |
          cd projects/user-service
          clojure -T:build uber
```

---

## 13) Troubleshooting Guide

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Illegal dependency on namespace" | You imported `core` instead of `interface`. Fix: Change import to `.interface` |
| "Circular dependencies detected" | Extract shared logic to new component with interface |
| "Missing component in project" | Add component to project's `deps.edn` |
| "Multiple components share same interface" | Include only one implementation per project |
| "Dev REPL is slow" | Remove unused bricks from `development/deps.edn` |
| "Cannot find brick" | Check `:local/root` paths are correct (../../ for projects, ../ for dev) |
| "Tests not running" | Ensure test paths are included in aliases |

### Debug Commands

```bash
# Show what changed
clojure -M:poly diff

# Show dependencies
clojure -M:poly deps

# Show brick usage
clojure -M:poly info :brick user

# Show project composition
clojure -M:poly info :project user-service

# Validate specific project
clojure -M:poly check :project user-service

# Run specific brick tests
clojure -M:poly test :brick user
```

---

## 14) Migration Strategy

### From Monolith to Polylith

1. **Identify boundaries** — Find natural seams in your monolith
2. **Extract interfaces** — Define component contracts before implementation
3. **Move incrementally** — One capability at a time
4. **Test continuously** — Ensure `poly check` passes at each step

```bash
# Example migration steps
poly create component name:user
# Move user logic to component
# Update imports to use interface
poly create base name:legacy-api
# Wire existing API to use new components
poly check
poly test
```

---

## 15) Pull Request Checklist (Enhanced)

### A) Structure & Dependencies
- [ ] Business logic lives in **components**; **bases** are adapters only
- [ ] Cross-component calls use **`...interface`** namespaces only (no `core` imports)
- [ ] **No component → base** dependencies
- [ ] **No circular deps** between components (validated by `poly check`)
- [ ] Exactly **one implementation per interface per project**
- [ ] Namespace & folder naming follows `com.mycompany.<brick>.(interface|core|api)`
- [ ] Each new/changed brick has a **README.md** with purpose, public APIs, examples

### B) Project Wiring
- [ ] Target **project(s)** updated: `projects/<proj>/deps.edn` includes required components and base
- [ ] **Development** REPL updated: `development/deps.edn` includes new bricks you're editing
- [ ] If you introduced a **port/interface**, each project selects **exactly one** adapter component
- [ ] Project builds and runs locally (`poly check`, `poly test`, plus run/build command)

### C) Tests & Quality
- [ ] **Component tests** cover the changed/added public interface functions
- [ ] **Base/API tests** cover HTTP/CLI contracts (routes, payloads, status codes)
- [ ] Integration tests added **only** where behavior spans multiple bricks and is critical
- [ ] `clojure -M:poly test` is **green** (changed/affected tests)
- [ ] Static analysis/lint (e.g., **clj-kondo**) passes if configured

### D) Security & Contracts
- [ ] Inputs validated at **base boundaries**; error responses are consistent
- [ ] No secrets/keys in code; configuration read via config components
- [ ] Any **public API change** is documented and versioned if required
- [ ] Side effects isolated behind interfaces (DB, HTTP, file I/O)

### E) Documentation & Developer Experience
- [ ] **CHANGELOG/Release notes** mention which **bricks** changed (use `poly diff`)
- [ ] Examples/snippets in READMEs updated
- [ ] Interface docstrings describe contracts clearly
- [ ] Breaking changes documented with migration path

### F) CI & Metadata
- [ ] CI runs `poly check` and `poly test`
- [ ] New dependencies properly declared and scoped (project vs brick level)
- [ ] Commit message references **bricks** and **interfaces** touched
- [ ] Git tags follow workspace convention (stable-*, v*, etc.)

---

## 16) Design Principles to Remember

* **Tease apart, then compose.** Smaller bricks → lower cognitive load
* **Separate what from how.** Interfaces communicate intent; implementations can change
* **Delay deployment decisions.** Keep development simple; compose for prod late
* **Monorepo ≠ monolith.** Many deployables; one place to develop
* **Think capabilities, not layers.** Organize by business function, not technical tier
* **Optimize for change.** Small interfaces make refactoring easier
* **Test at the boundaries.** Focus on contracts, not internals

---

## 17) Quick Troubleshooting

* **"I imported another component's core and `poly check` fails"** → Switch to `that.component.interface`
* **"Prod project can't build: missing component"** → Add the component that implements the interface you call
* **"Need two impls of the same interface"** → Create a second component, pick per-project
* **"Dev is slow/noisy"** → Trim `development/deps.edn` to only the bricks you're editing
* **"Can't find namespace at runtime"** → Check `:local/root` paths and that brick is in deps.edn
* **"Interface changes break other components"** → Version interfaces or add backwards compatibility

---

## 18) What Claude Should Ask (Only When Needed)

* *Name & purpose* of the new brick or endpoint
* *Which project(s)* should include the change (deployables affected)?
* *Interface contract* (function names, args, return types) if unclear
* *Port or implementation?* when creating storage/external adapters
* *Breaking change?* when modifying existing interfaces

> Otherwise, follow the defaults above and proceed without blocking.

---

## 19) Anti-Patterns & Fixes

| Anti-Pattern | Fix |
|--------------|-----|
| **Fat base** (business logic in bases) | Move logic into component; base calls component interface |
| **Importing another component's `core`** | Replace with `...interface` import; add interface if missing |
| **Leaky infrastructure** (DB/HTTP in core) | Hide effects behind port interface; provide adapters |
| **God component** | Split by capability; extract new components with focused interfaces |
| **Over-broad interface** | Break into smaller, coherent interfaces (e.g., `user.read` and `user.write`) |
| **Dev project pulls everything** | Trim `development/deps.edn` to active bricks only |
| **Shared mutable state** | Pass state explicitly or use component for state management |
| **Interface exposes implementation** | Return data, not DB records; use transfer objects |

---

## 20) "Where Does This Code Go?" (Decision Guide)

```
Is it external protocol handling? → Base
  └─ HTTP routes, CLI parsing, queue consumers, etc.

Is it business logic/rules? → Component
  └─ User management, payment processing, order fulfillment, etc.

Is it infrastructure/effects? → Component (implementing a port)
  └─ Database, cache, external APIs, file system, etc.

Is it pure utilities? → Library component
  └─ Date formatting, string utils, data transformations, etc.

Is it cross-cutting concerns? → Component with interface
  └─ Logging, metrics, authentication, authorization, etc.

Is it deployment configuration? → Project
  └─ Environment-specific settings, dependency selection, etc.
```

---

## 21) CI/CD Recommendations

### Essential CI Steps
1. `clojure -M:poly check` — Fail fast on structure violations
2. `clojure -M:poly test` — Run changed/affected tests
3. `clojure -M:poly info :changed` — Document what changed
4. Build affected projects only (use `poly` to detect)

### Advanced CI/CD
```bash
# Detect changed projects
CHANGED_PROJECTS=$(clojure -M:poly ws get:changes:changed-projects)

# Build only changed projects
for project in $CHANGED_PROJECTS; do
  cd projects/$project
  clojure -T:build uber
  cd ../..
done

# Tag stable points
if [ "$BRANCH" = "main" ]; then
  git tag stable-$(date +%Y%m%d-%H%M%S)
fi
```

---

## 22) Releases & Workspace Hygiene

### Version Management
* **Stable tags:** `stable-YYYYMMDD-HHMMSS` for CI references
* **Release tags:** `v1.2.3` for production releases
* **Brick versions:** Keep interfaces stable; version breaking changes

### Documentation Standards
* Each brick has `README.md` with:
  - Purpose and responsibilities
  - Interface functions with examples
  - Configuration requirements
  - Dependencies on other interfaces

### Code Organization
* Consistent namespace structure: `com.mycompany.<brick>.<type>`
* One public interface per component
* Implementation details in `core` or sub-namespaces

---

## 23) Refactoring Playbook

### Extract Port
When infrastructure leaks into core:
1. Create port component with interface definition
2. Move infrastructure to adapter component
3. Update projects to include chosen adapter
4. Run `poly check` and fix imports

### Split Component
When component grows too large:
1. Identify cohesive subsets of functionality
2. Create new component(s) with focused interfaces
3. Update original component to delegate or remove functions
4. Update all callers to use appropriate interfaces

### Converge Interfaces
When components have similar interfaces:
1. Align function names and signatures
2. Create shared port interface if appropriate
3. Update implementations to match
4. Projects can now swap implementations

### Safe Renaming
1. Update namespace in src and test files
2. Update all `:local/root` references in deps.edn files
3. Update all imports in other bricks
4. Run `poly check` to catch stragglers
5. Update documentation

---

## 24) Worked Examples

### Example 1: Add Payment Processing

```bash
# 1. Create payment component
poly create component name:payment

# 2. Create payment gateway port
poly create component name:payment-gateway

# 3. Create Stripe adapter
poly create component name:payment-gateway-stripe

# 4. Create test adapter for dev
poly create component name:payment-gateway-fake

# 5. Update base to expose payment endpoint
# Edit bases/rest-api/src/.../api.clj

# 6. Wire up projects
# Dev: payment + payment-gateway-fake
# Prod: payment + payment-gateway-stripe

# 7. Validate
clojure -M:poly check
clojure -M:poly test
```

### Example 2: Extract User Service from Monolith

```bash
# 1. Identify user-related code in monolith

# 2. Create user component with interface
poly create component name:user

# 3. Move business logic to user/core.clj
# Keep interface minimal and stable

# 4. Create legacy adapter base
poly create base name:legacy-api

# 5. Update legacy code to call user.interface

# 6. Create new REST API base
poly create base name:user-api

# 7. Create service project
poly create project name:user-service

# 8. Gradually migrate clients to new service
```

### Example 3: Add Event-Driven Processing

```bash
# 1. Create events port
poly create component name:events

# 2. Create Kafka adapter
poly create component name:events-kafka

# 3. Create in-memory adapter for testing
poly create component name:events-memory

# 4. Create event processor base
poly create base name:event-processor

# 5. Create processor service project
poly create project name:event-processor-service

# 6. Components publish events via events.interface
# Base consumes events and delegates to components
```

---

## 25) Performance Optimization

### REPL Performance
* Keep `development/deps.edn` minimal
* Use `clojure.tools.namespace.repl/refresh` selectively
* Consider separate dev profiles for different features

### Build Performance
* Cache dependencies in CI
* Build only changed projects
* Use parallel test execution where possible

### Runtime Performance
* Components should be stateless for easy scaling
* Use ports/adapters for caching strategies
* Profile before optimizing

---

## 26) Security Best Practices

### Component Security
* Validate at interface boundaries
* Don't trust data between components
* Use spec/schema for contract validation

### Base Security
* Authenticate/authorize at base level
* Sanitize all external inputs
* Use secure defaults

### Configuration Security
* Never commit secrets
* Use environment variables or secret management
* Provide secure defaults

---

## 27) Testing Strategies

### Unit Testing (Components)
```clojure
;; Test through interfaces only
(deftest component-interface-test
  (testing "interface contract"
    (is (= expected (interface-fn input)))))
```

### Integration Testing (Cross-Component)
```clojure
;; Test component interactions
(deftest integration-test
  (with-test-system
    (testing "components work together"
      (is (= expected (workflow-test))))))
```

### Contract Testing (Bases)
```clojure
;; Test external contracts
(deftest api-contract-test
  (testing "API responds correctly"
    (let [response (call-api request)]
      (is (= 200 (:status response))))))
```

### Property-Based Testing
```clojure
(defspec component-properties
  100
  (prop/for-all [input gen/any]
    (satisfies-invariant? (interface-fn input))))
```

---

## 28) Monitoring & Observability

### Metrics Component
```clojure
;; components/metrics/src/.../interface.clj
(ns com.mycompany.metrics.interface)

(defn increment! [metric])
(defn timing! [metric f])
(defn gauge! [metric value])
```

### Logging Component
```clojure
;; components/logging/src/.../interface.clj
(ns com.mycompany.logging.interface)

(defn debug [msg data])
(defn info [msg data])
(defn error [msg data])
```

### Tracing Component
```clojure
;; components/tracing/src/.../interface.clj
(ns com.mycompany.tracing.interface)

(defn span [name f])
(defn add-context! [k v])
```

---

## 29) Advanced Patterns

### Saga Pattern
```clojure
;; Component for distributed transactions
(ns com.mycompany.saga.interface)

(defn start-saga [steps])
(defn compensate [saga-id])
```

### Circuit Breaker
```clojure
;; Component for fault tolerance
(ns com.mycompany.circuit-breaker.interface)

(defn wrap [f options])
(defn reset! [name])
```

### Feature Flags
```clojure
;; Component for feature management
(ns com.mycompany.features.interface)

(defn enabled? [feature user])
(defn get-variant [feature user])
```

---

## 30) Final Prompt Snippets for Claude Code

### Add Capability
> Create `components/<name>` with a minimal `...interface` that delegates to `core`. Write component tests for the new public functions. Wire the component into `development/deps.edn` and the target project's `deps.edn`.

### Create Adapter
> Implement a new adapter component that exposes the same interface as `<port>`. Update the appropriate project to include exactly one implementation of that interface.

### Expose Endpoint
> In `bases/<http-base>`, add a handler that validates input and delegates to `<component>.interface`. Add base tests for contract and component tests for behavior.

### Fix Dependency Violation
> Replace any import of another component's `core` with its `...interface`. If it doesn't exist, create one and delegate.

### Extract Port
> When you see infrastructure in a component's core, create a port interface and move the infrastructure to an adapter component. Update projects to include appropriate adapters.

### Split Component
> When a component has too many responsibilities, identify cohesive subsets and extract them into focused components with clear interfaces.

---

**End of Guide. Keep it small, keep it clean, keep it composable.**