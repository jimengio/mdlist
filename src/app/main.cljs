
(ns app.main
  (:require [respo.core :refer [render! clear-cache! realize-ssr!]]
            [app.comp.container :refer [comp-container]]
            [app.updater :refer [updater]]
            [app.schema :as schema]
            [reel.util :refer [listen-devtools!]]
            [reel.core :refer [reel-updater refresh-reel]]
            [reel.schema :as reel-schema]
            [cljs.reader :refer [read-string]]
            [app.config :as config]
            [cumulo-util.core :refer [repeat!]]
            [clojure.string :as string]
            [app.files :refer [files-map]]))

(defonce *reel
  (atom (-> reel-schema/reel (assoc :base schema/store) (assoc :store schema/store))))

(defn dispatch! [op op-data]
  (when config/dev? (println "Dispatch:" op))
  (reset! *reel (reel-updater updater @*reel op op-data)))

(def mount-target (.querySelector js/document ".app"))

(defn on-window-keydown [event]
  (case (.-code event)
    "Slash"
      (when (not= "search-box" (.-className (.-activeElement js/document)))
        (let [target (.querySelector js/document ".search-box")]
          (.select target)
          (.preventDefault event)))
    :else))

(defn persist-storage! []
  (.setItem js/localStorage (:storage-key config/site) (pr-str (:store @*reel))))

(defn render-app! [renderer]
  (renderer mount-target (comp-container @*reel) #(dispatch! %1 %2)))

(def ssr? (some? (js/document.querySelector "meta.respo-ssr")))

(defn main! []
  (println "Running mode:" (if config/dev? "dev" "release"))
  (if ssr? (render-app! realize-ssr!))
  (render-app! render!)
  (add-watch *reel :changes (fn [] (render-app! render!)))
  (listen-devtools! "a" dispatch!)
  (.addEventListener js/window "beforeunload" persist-storage!)
  (.addEventListener js/window "keydown" #(on-window-keydown %))
  (repeat! 60 persist-storage!)
  (let [initial-page (string/replace
                      (js/decodeURIComponent (subs js/location.hash 1))
                      "_"
                      " ")]
    (if (some? (get files-map initial-page))
      (do (dispatch! :filter initial-page) (dispatch! :select 0))
      (let [raw (.getItem js/localStorage (:storage-key config/site))]
        (when (some? raw) (dispatch! :hydrate-storage (read-string raw))))))
  (println "App started."))

(defn reload! []
  (clear-cache!)
  (reset! *reel (refresh-reel @*reel schema/store updater))
  (println "Code updated."))
