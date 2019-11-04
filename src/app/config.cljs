
(ns app.config )

(def cdn?
  (cond
    (exists? js/window) false
    (exists? js/process) (= "true" js/process.env.cdn)
    :else false))

(def dev?
  (let [debug? (do ^boolean js/goog.DEBUG)]
    (cond
      (exists? js/window) debug?
      (exists? js/process) (not= "true" js/process.env.release)
      :else true)))

(def site
  {:dev-ui "http://localhost:8100/main.css",
   :release-ui "https://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "https://fr.jimu.io/cdn/mdlist/",
   :title "Docs",
   :icon "https://cdn.tiye.me/logo/jimeng-360x360.png",
   :storage-key "mdlist"})
