(ns leiningen.new.porpus-proj
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "porpus-proj"))

(defn porpus-proj
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' porpus-proj project.")
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["src/clj/{{sanitized}}/handler.clj" (render "src/clj/porpus/handler.clj" data)]
             ["src/clj/{{sanitized}}/server.clj" (render "src/clj/porpus/server.clj" data)]
             ["src/cljc/{{sanitized}}/util.cljc" (render "src/cljc/porpus/util.cljc" data)]
             ["src/cljs/{{sanitized}}/core.cljs" (render "src/cljs/porpus/core.cljs" data)]
             ["resources/public/css/site.css" (render "resources/public/css/site.css" data)]
             ["resources/public/css/site.min.css" (render "resources/public/css/site.min.css" data)]
             ["env/dev/clj/{{sanitized}}/middleware.clj" (render "env/dev/clj/porpus/middleware.clj" data)]
             ["env/dev/clj/{{sanitized}}/repl.clj" (render "env/dev/clj/porpus/repl.clj" data)]
             ["env/dev/clj/user.clj" (render "env/dev/clj/user.clj" data)]
             ["env/dev/cljs/{{sanitized}}/dev.cljs" (render "env/dev/cljs/porpus/dev.cljs" data)]
             ["env/prod/clj/{{sanitized}}/middleware.clj" (render "env/prod/clj/porpus/middleware.clj" data)]
             ["env/prod/cljs/{{sanitized}}/prod.cljs" (render "env/prod/cljs/porpus/prod.cljs" data)]
             )))
