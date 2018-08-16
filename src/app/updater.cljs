
(ns app.updater (:require [respo.cursor :refer [mutate]]))

(defn updater [store op op-data op-id op-time]
  (case op
    :states (update store :states (mutate op-data))
    :select (assoc store :selected op-data)
    :filter (assoc store :filter op-data)
    :hydrate-storage op-data
    store))
