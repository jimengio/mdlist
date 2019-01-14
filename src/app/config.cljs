
(ns app.config )

(def bundle-builds #{"release" "local-bundle"})

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
   :release-ui "http://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "http://cdn.tiye.me/mdlist/",
   :cdn-folder "tiye.me:cdn/mdlist",
   :title "Docs",
   :icon "http://cdn.tiye.me/logo/jimeng-360x360.png",
   :storage-key "mdlist",
   :upload-folder "tiye.me:repo/chenyong/mdlist/"})
