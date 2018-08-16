
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.macros
             :refer
             [defcomp cursor-> action-> mutation-> list-> <> div button textarea span input]]
            [verbosely.core :refer [verbosely!]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            [app.files :refer [files-map]]
            [respo-md.comp.md :refer [comp-md-block]]
            [clojure.string :as string]))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       selected (:selected store)
       query (:filter store)]
   (div
    {:style (merge ui/global ui/row ui/fullscreen)}
    (div
     {:style (merge
              ui/column
              {:overflow :auto,
               :padding "16px 0px 200px 0px",
               :border-right (str "1px solid " (hsl 0 0 80)),
               :width 320,
               :white-space :nowrap})}
     (div
      {:style {:padding "0 16px"}}
      (input
       {:style (merge ui/input {:width "100%"}),
        :placeholder "Filter...",
        :value query,
        :on-input (fn [e d! m!] (d! :filter (:value e)))}))
     (=< nil 16)
     (list->
      {:style (merge ui/flex)}
      (->> (keys files-map)
           (filter
            (fn [file-path]
              (->> (string/split query " ")
                   (some
                    (fn [piece]
                      (or (string/blank? piece)
                          (and (not (string/blank? piece))
                               (string/includes? file-path piece))))))))
           (sort)
           (map
            (fn [file-path]
              [file-path
               (div
                {:style (merge
                         {:padding "0 16px", :line-height "32px", :cursor :pointer}
                         (when (= file-path selected) {:background-color (hsl 0 0 90)})),
                 :on-click (fn [e d! m!] (d! :select file-path))}
                (<> (-> file-path (string/replace "/" " ") (string/replace ".md" ""))))])))))
    (if (nil? selected)
      (div {:style (merge ui/flex ui/center)} (<> "No selection"))
      (div
       {:style (merge ui/flex {:overflow :auto, :padding "32px 32px 200px 32px"})}
       (comp-md-block (get files-map selected) {})))
    (when dev? (comp-inspect "Co" store {:bottom 0}))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
