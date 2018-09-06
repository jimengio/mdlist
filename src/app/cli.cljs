
(ns app.cli
  (:require ["fs" :as fs]
            ["path" :as path]
            ["child_process" :as cp]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]
            [cljs.core.async :refer [put! chan <! >! timeout close!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn get-chan-file [x]
  (let [chan-file (chan)]
    (fs/readFile
     x
     "utf8"
     (fn [err content]
       (when (some? err) (.error js/console "Error reading file:" err))
       (go (>! chan-file (if (some? err) nil content)))))
    chan-file))

(def jimu-folder "/Users/chen/work/jimu/src/pkg.jimu.io")

(defn get-chan-file-time [jimu-folder x]
  (let [chan-time (chan)]
    (cp/exec
     (str "cd " jimu-folder "&& git log -1 --format=\"%aI\" -- " x)
     (fn [err stdout stderr]
       (when (some? err) (.error js/console "Error get time:" err))
       (go (>! chan-time (if (some? err) nil (string/trim stdout))))))
    chan-time))

(defn grab-files! []
  (go
   (let [start-time (.now js/Date)
         channels (as->
                   (.toString (cp/execSync (str "find " jimu-folder " | grep .md$")))
                   xx
                   (string/split xx "\n")
                   (filter
                    (fn [x]
                      (and (string/includes? x ".md")
                           (not (string/includes? x "vendor"))
                           (not (string/includes? x "node_modules"))))
                    xx)
                   (take 500 xx)
                   (map
                    (fn [x]
                      (let [chan-pair (chan 1)]
                        (go
                         (let [content (<! (get-chan-file x))
                               time (<! (get-chan-file-time jimu-folder x))]
                           (>!
                            chan-pair
                            [(string/replace x jimu-folder "")
                             {:content content, :time time}])))
                        chan-pair))
                    xx))]
     (loop [xs channels, acc []]
       (if (empty? xs)
         (let [data (into {} acc)]
           (fs/writeFileSync
            "resource/app/files.cljs"
            (str "(ns app.files)\n\n(def files-map\n" (pr-str data) "\n)"))
           (println "Finished in" (/ (- (.now js/Date) start-time) 1000) "seconds"))
         (let [cursor (first xs), pair (<! cursor)] (recur (rest xs) (conj acc pair))))))))

(defn main! [] (println "started") (grab-files!))

(defn reload! [] (.write js/process.stdout (read-string "\"\\033c\"")) (grab-files!))
