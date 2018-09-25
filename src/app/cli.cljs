
(ns app.cli
  (:require ["fs" :as fs]
            ["path" :as path]
            ["child_process" :as cp]
            [clojure.string :as string]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]
            [cljs.core.async :refer [put! chan <! >! timeout close! go go-loop]]))

(defn get-chan-file [x]
  (let [chan-file (chan)]
    (fs/readFile
     x
     "utf8"
     (fn [err content]
       (when (some? err) (.error js/console "Error reading file:" err))
       (go (>! chan-file (if (some? err) (str "Error reading file: " x) content)))))
    chan-file))

(def jimu-folder "/Users/chen/work/jimu/src/pkg.jimu.io")

(defn get-chan-file-time [jimu-folder x]
  (let [chan-time (chan)]
    (cp/exec
     (str "cd " jimu-folder "&& git log -1 --format=\"%aI\" -- " x)
     (fn [err stdout stderr]
       (when (some? err) (.error js/console "Error get time:" err))
       (go (>! chan-time (if (some? err) (str "Error get time: " x) (string/trim stdout))))))
    chan-time))

(defn wait-all! [channels]
  (let [chan-batched (chan 1)]
    (go
     (loop [xs channels, acc []]
       (if (empty? xs)
         (>! chan-batched acc)
         (let [cursor (first xs), pair (<! cursor)] (recur (rest xs) (conj acc pair))))))
    chan-batched))

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
                   (map
                    (fn [x]
                      (let [chan-pair (chan 1)]
                        (go
                         (let [content (<! (get-chan-file x))
                               time (<! (get-chan-file-time jimu-folder x))]
                           (>!
                            chan-pair
                            [(-> x
                                 (string/replace jimu-folder "")
                                 (string/replace "/" " ")
                                 (string/replace ".md" "")
                                 (string/replace "_" "-"))
                             {:content content, :time time}])))
                        chan-pair))
                    xx))
         results (<! (wait-all! channels))]
     (let [data (into {} results)]
       (fs/writeFileSync
        "resource/app/files.cljs"
        (str "(ns app.files)\n\n(def files-map\n" (pr-str data) "\n)"))
       (println "Finished in" (/ (- (.now js/Date) start-time) 1000) "seconds")))))

(defn main! [] (println "Start grabbing files...") (grab-files!))

(defn reload! [] (.write js/process.stdout (read-string "\"\\033c\"")) (grab-files!))
