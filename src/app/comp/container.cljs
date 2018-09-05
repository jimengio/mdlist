
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
            [clojure.string :as string]
            ["dayjs" :as dayjs]
            ["fuzzy" :as fuzzy]))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       selected (:selected store)
       query (:filter store)
       visible-files (->> (keys files-map)
                          (filter
                           (fn [file-path]
                             (->> (string/split query " ")
                                  (some
                                   (fn [piece]
                                     (or (string/blank? piece)
                                         (and (not (string/blank? piece))
                                              (fuzzy/test piece file-path))))))))
                          (sort))]
   (div
    {:style (merge ui/global ui/row ui/fullscreen)}
    (div
     {:style (merge
              ui/column
              {:overflow :auto,
               :border-right (str "1px solid " (hsl 0 0 94)),
               :width 320,
               :white-space :nowrap})}
     (div
      {:style {:padding 16, :border-bottom (str "1px solid " (hsl 0 0 90))}}
      (input
       {:class-name "search-box",
        :style (merge ui/input {:width "100%", :font-family ui/font-code}),
        :placeholder "Filter...",
        :value query,
        :on-input (fn [e d! m!] (d! :filter (:value e)) (d! :select 0)),
        :on-keydown (fn [e d! m!]
          (case (:code e)
            "ArrowDown"
              (do (d! :move-down (count visible-files)) (.preventDefault (:event e)))
            "ArrowUp" (do (d! :move-up (count visible-files)) (.preventDefault (:event e)))
            :else))}))
     (list->
      {:style (merge ui/flex {:overflow :auto, :padding-bottom 120, :padding-top 16})}
      (->> visible-files
           (map-indexed
            (fn [idx file-path]
              [file-path
               (div
                {:style (merge
                         {:padding "0 16px",
                          :line-height "32px",
                          :cursor :pointer,
                          :width "100%",
                          :overflow :auto}
                         (when (= idx selected) {:background-color (hsl 0 0 90)})),
                 :on-click (fn [e d! m!] (d! :select idx))}
                (<> (-> file-path (string/replace "/" " ") (string/replace ".md" ""))))])))))
    (if (nil? selected)
      (div {:style (merge ui/flex ui/center)} (<> "No selection"))
      (let [file (get files-map (get (vec visible-files) selected))]
        (div
         {:style (merge ui/flex {:overflow :auto, :padding "32px 32px 200px 32px"})}
         (div
          {:style {:font-family ui/font-fancy, :font-size 16, :color (hsl 0 0 80)}}
          (<> (str "Last modified at " (.format (dayjs (:time file)) "YYYY-MM-DD hh:mm"))))
         (comp-md-block (:content file) {}))))
    (when dev? (comp-inspect "Co" store {:bottom 0}))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
