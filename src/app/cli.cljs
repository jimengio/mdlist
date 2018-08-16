
(ns app.cli
  (:require ["fs" :as fs]
            ["path" :as path]
            ["child_process" :as cp]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]))

(def jimu-folder "/Users/chen/work/jimu/src/pkg.jimu.io")

(defn grab-files! []
  (.write js/process.stdout (read-string "\"\\033c\""))
  (let [data (as->
              (.toString (cp/execSync (str "find " jimu-folder " | grep .md$")))
              xx
              (string/split xx "\n")
              (filter
               (fn [x]
                 (and (string/includes? x ".md")
                      (not (string/includes? x "vendor"))
                      (not (string/includes? x "node_modules"))))
               xx)
              (map
               (fn [x] [(string/replace x jimu-folder "") (fs/readFileSync x "utf8")])
               xx)
              (into {} xx))]
    (fs/writeFileSync
     "resource/app/files.cljs"
     (str "(ns app.files)\n\n(def files-map\n" (pr-str data) "\n)"))))

(defn main! [] (println "started") (grab-files!))

(defn reload! [] (grab-files!))
