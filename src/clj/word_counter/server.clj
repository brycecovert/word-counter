(ns word-counter.server
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :refer [deftemplate]]
            [net.cgrand.tagsoup :as tagsoup]
            [net.cgrand.reload :refer [auto-reload]]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [ring.util.io :refer [string-input-stream]]
            [clj-http.client :as client]))

(def state (atom (list)))

(defn edn-response [data]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn add-ids [urls]
  (map #(assoc %1 :id %2) urls (range)))

(deftemplate page (io/resource "index.html") []
  [:body] identity)

(defn get-counts [state]
  (->> state
       add-ids
       (take 10)))

(defn- get-strings [tag & so-far]
  (let [so-far (or so-far [])]
    (if (nil? (:content tag))
      []
      (if (and (every? string? (:content tag))
               (not (#{:script :style} (:tag tag))))
        (:content tag)
        (mapcat get-strings (:content tag))))))

(defn get-all-words-from-body [body]
  (let [all-content (->> body
                         string-input-stream
                         tagsoup/parser
                         (mapcat get-strings)
                         (str/join " "))]
    (->> (str/split all-content #"\W")
         (filter (complement #{""}))
         (map str/lower-case))))

(defn count-words [all-words]
  (let [counted-words (reduce (fn [words word]
                                (update-in words [word] #(inc (or % 0))))
                              {}
                              all-words)]
    (->> counted-words
         (map (fn [[k v]]
                {:word k :count v}))
         (sort-by (comp - :count))
         (take 10))))

(defn count-url [url]
  (let [all-words (-> (client/get url {:socket-timeout 1000 :conn-timeout 1000})
                      :body
                      get-all-words-from-body)]
    {:url url
     :words (count-words all-words)}))

(defroutes routes
  (resources "/")

  (GET "/" req (page))

  (GET "/count" []
       (edn-response (get-counts @state))) 

  (POST "/count" [url]
        (when-not (seq (filter #(= (:url %) url) @state)) 
          (swap! state #(take 10 (conj % (count-url url)))))
        (edn-response (get-counts @state))))

(def http-handler
  (wrap-edn-params
   (reload/wrap-reload (wrap-defaults #'routes api-defaults))))

