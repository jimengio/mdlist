
(ns app.config (:require [app.util :refer [get-env!]]))

(def bundle-builds #{"release" "local-bundle"})

(def dev?
  (if (exists? js/window)
    (do ^boolean js/goog.DEBUG)
    (not (contains? bundle-builds (get-env! "mode")))))

(def site
  {:storage "mdlist",
   :dev-ui "http://localhost:8100/main.css",
   :release-ui "http://cdn.tiye.me/favored-fonts/main.css",
   :cdn-url "http://cdn.tiye.me/mdlist/",
   :cdn-folder "tiye.me:cdn/mdlist",
   :title "Docs",
   :icon "http://cdn.tiye.me/logo/jimeng-360x360.png",
   :upload-folder "tiye.me:repo/chenyong/mdlist/"})
