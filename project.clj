(defproject word-counter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj"]
  :repl-options {:timeout 200000} ;; Defaults to 30000 (30 seconds)

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2511" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [compojure "1.3.1"]
                 [enlive "1.1.5"]
                 [reagent "0.5.0"]
                 [fogus/ring-edn "0.2.0"]
                 [secretary "1.2.3"] 
                 [cljs-http "0.1.30"]
                 [clj-http "1.1.2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :plugins [[lein-cljsbuild "1.0.3"]
           [lein-ring "0.9.3"]]
  :ring {:handler word-counter.server/http-handler}
  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :source-map    "resources/public/js/out.js.map"
                                        :optimizations :advanced}}}}
  :profiles {:dev {:test-paths ["test/clj"]}})
