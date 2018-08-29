
(ns app.cli
  (:require ["fs" :as fs]
            ["path" :as path]
            ["child_process" :as cp]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]))

(def jimu-folder "/Users/chen/work/jimu/src/pkg.jimu.io")

(defn grab-files! []
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
               (fn [x]
                 [(string/replace x jimu-folder "")
                  {:content (fs/readFileSync x "utf8"),
                   :time (string/trim
                          (.toString
                           (cp/execSync
                            (str "cd " jimu-folder "&& git log -1 --format=\"%aI\" -- " x))))}])
               xx)
              (into {} xx))]
    (fs/writeFileSync
     "resource/app/files.cljs"
     (str "(ns app.files)\n\n(def files-map\n" (pr-str data) "\n)"))))

(defn main! [] (println "started") (grab-files!))

(defn reload! [] (.write js/process.stdout (read-string "\"\\033c\"")) (grab-files!))
