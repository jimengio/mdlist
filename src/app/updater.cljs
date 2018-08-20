
(ns app.updater (:require [respo.cursor :refer [mutate]]))

(defn updater [store op op-data op-id op-time]
  (case op
    :states (update store :states (mutate op-data))
    :select (assoc store :selected op-data)
    :filter (assoc store :filter op-data)
    :move-up
      (let [size op-data]
        (update store :selected (fn [idx] (if (pos? idx) (dec idx) (dec size)))))
    :move-down
      (let [size op-data]
        (update store :selected (fn [idx] (if (< (inc idx) size) (inc idx) 0))))
    :hydrate-storage op-data
    store))
