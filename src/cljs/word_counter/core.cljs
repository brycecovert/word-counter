(ns word-counter.core
   (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            )
  (:import goog.History))

(secretary/set-config! :prefix "#")

(def state (reagent/atom {:url ""
                          :recent-counts (list)}))

(defn client-redirect [token]
  (let [token (str "#" token)]
    (if (not= (.-location js/window) token)
      (set! (.-location js/window) token))))

(defn check-url [url]
  (re-matches #"http[s]*://.+\..+" url))

(defn find-url-id [url]
  (-> (filter #(= (:url %) url) (:recent-counts @state))
      first
      :id))

(defn count-words* [url]
  (go
   (swap! state assoc :warning nil)
   (let [response (<! (http/post "/count" {:edn-params {:url url}} ))] 
     (if (= 200 (:status response))
       (do (swap! state assoc :recent-counts (:body response))
           (client-redirect (str "/" (find-url-id url))))
       (swap! state assoc :warning "Sorry, it looks like we can't look that url up.")))))

(defn count-words [e]
  (if-let [url (check-url (:url @state))]
    (count-words* url)
    (swap! state assoc :warning "Sorry, it looks like that's not a valid url."))
  (.preventDefault e)
  false)

(defn header []
  [:div
   [:h1 "Word counter"]
   (when-let [warning (:warning @state)]
     [:div.alert.alert-danger warning])])

(defn recent-counts []
  [:div#recent-searches
   [:h2 "Recent Counts"]
   [:ul
    (for [{:keys [id url words]} (:recent-counts @state)
          :let [first-word (first words)]]
      ^{:key id}
      [:li {:style {:font-size "20px"}}
       [:a {:href (str "#/" id )} url]
       [:span.label.label-default.label-info  (:word first-word)
        " "
        [:span.badge (:count first-word)]]])]])

(defn count-form* []
  [:div
   [header]
   [:div
    [:form
     [:h2 "Count Words"]
     [:label {:for "url"} "Want to count the words on a site? Enter a URL:"]
     [:div.row
      [:div.col-xs-11
       [:input.form-control.input-lg {:type "text" :name "url" :id "url" :placeholder "http://www.google.com" :value (:url @state) :on-change #(swap! state assoc :url (-> % .-target .-value))}]]
      [:div.col-xs-1
       [:input.btn.btn-primary.input-lg.form-control {:type "submit" :value "count" :on-click count-words}]]]
     [:div.alert.alert-info {:style {:margin-top "15px"}}
      [:div.col-xs-3 "Struggling to think of a site? Try these:"]
      [:ul.list-inline
       [:li
        [:a {:on-click #(swap! state assoc :url "http://americanliterature.com/author/herman-melville/book/moby-dick-or-the-whale/chapter-1-loomings") :href "#"} "Moby dick chapter one"]] ", "
       [:li
        [:a {:on-click #(swap! state assoc :url "http://americanliterature.com/author/herman-melville/book/moby-dick-or-the-whale/chapter-2-the-carpet-bag") :href "#"} "Moby dick chapter two"] ]]]]]
   [recent-counts]])

(def count-form (with-meta count-form*
            {:component-did-mount
             (fn []
               (go
                (let [response (<! (http/get "/count"))
                      result (:body response)]
                  (swap! state #(assoc-in % [:recent-counts] result)))))}))

(defn count-result [{:keys [url words]}]
  [:div
   [header] 
   [:div 
     [:h3 "Url: " ]
    [:p url]
     [:ul.list-inline {:style {:font-size "20px"}}
      (for [word words]
        ^{:key word}
        [:li 
         [:span.label.label-default.label-info  (:word word)
          " "
          [:span.badge (:count word)]]])]]
   [:div
    [:a {:href "#"} [:h4 "Back"]]]])

(defroute index "/" []
  (reagent/render-component [count-form] (.getElementById js/document "app")))

(defroute view-count "/:id" [id]
  (let [id (js/parseInt id)]
    (reagent/render-component [count-result (some #(when (= id (:id %)) %)
                                                   (:recent-counts @state))]
                              (.getElementById js/document "app"))))


(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (doto h (.setEnabled true)))

(defn main []
  (secretary/dispatch! "/"))
