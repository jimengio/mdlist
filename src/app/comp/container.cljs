
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core
             :refer
             [defcomp
              cursor->
              action->
              mutation->
              list->
              <>
              div
              button
              textarea
              span
              input
              a]]
            [respo.comp.space :refer [=<]]
            [reel.comp.reel :refer [comp-reel]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo-md.comp.md :refer [comp-md]]
            [app.config :refer [dev?]]
            [app.files :refer [files-map]]
            [respo-md.comp.md :refer [comp-md-block]]
            [clojure.string :as string]
            ["dayjs" :as dayjs]
            ["copy-text-to-clipboard" :as copy!]
            [fuzzy-filter.core :refer [parse-by-letter parse-by-word]]
            [fuzzy-filter.comp.visual :refer [comp-visual]]
            [feather.core :refer [comp-i]]))

(defcomp
 comp-empty
 ()
 (div
  {:style (merge
           ui/flex
           ui/center
           {:font-family ui/font-fancy,
            :color (hsl 0 0 80),
            :font-weight 300,
            :font-size 24,
            :user-select :none})}
  (<> "No selection")))

(defcomp
 comp-container
 (reel)
 (let [store (:store reel)
       states (:states store)
       selected (:selected store)
       query (:filter store)
       visible-file-infos (->> (keys files-map)
                               (map
                                (fn [file-path]
                                  (let [result (parse-by-word file-path query)] result)))
                               (filter (fn [result] (:matches? result)))
                               (sort-by (fn [result] (count (:chunks result)))))]
   (div
    {:style (merge ui/global ui/row ui/fullscreen)}
    (div
     {:style (merge
              ui/column
              {:overflow :auto,
               :border-right (str "1px solid " (hsl 0 0 94)),
               :width 400,
               :white-space :nowrap})}
     (div
      {:style {:padding 8, :border-bottom (str "1px solid " (hsl 0 0 90))}}
      (input
       {:class-name "search-box",
        :style (merge ui/input {:width "100%", :font-family ui/font-code, :border :none}),
        :placeholder "...",
        :value query,
        :on-input (fn [e d! m!] (d! :filter (:value e)) (d! :select 0)),
        :on-keydown (fn [e d! m!]
          (case (:code e)
            "ArrowDown"
              (do (d! :move-down (count visible-file-infos)) (.preventDefault (:event e)))
            "ArrowUp"
              (do (d! :move-up (count visible-file-infos)) (.preventDefault (:event e)))
            :else))}))
     (list->
      {:style (merge ui/flex {:overflow :auto, :padding-bottom 120, :padding-top 16})}
      (->> visible-file-infos
           (map-indexed
            (fn [idx file-info]
              (let [file-path (:text file-info)]
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
                  (comp-visual
                   (:chunks file-info)
                   {:style-rest {:color (hsl 0 0 70)},
                    :style-hitted {:color (hsl 0 0 0), :font-weight :normal}}))]))))))
    (if (nil? selected)
      (comp-empty)
      (let [file (get files-map (:text (get (vec visible-file-infos) selected)))]
        (if (some? file)
          (div
           {:style (merge ui/flex {:overflow :auto, :padding "32px 32px 200px 32px"})}
           (div
            {:style {:font-family ui/font-fancy, :font-size 16, :color (hsl 0 0 80)}}
            (<> (str "Last modified at " (.format (dayjs (:time file)) "YYYY-MM-DD hh:mm")))
            (=< 8 nil)
            (a
             {:style {:cursor :pointer},
              :on-click (fn [e d! m!]
                (set!
                 js/location.hash
                 (string/replace (:text (get (vec visible-file-infos) selected)) " " "_"))
                (copy! js/location.href))}
             (comp-i :link 14 (hsl 200 80 30))))
           (comp-md-block (:content file) {:style {:white-space :pre-wrap}}))
          (comp-empty))))
    (when dev? (comp-inspect "Co" store {:bottom 0}))
    (when dev? (cursor-> :reel comp-reel states reel {})))))
