(set-env!
  :source-paths #{"src"}
  :resource-paths #{"resources"}
  :dependencies '[[adzerk/boot-cljs "1.7.228-1" :scope "test"]
                  [adzerk/boot-reload "0.4.12" :scope "test"]
                  [pandeiro/boot-http "0.7.3" :scope "test"]
                  ; repl
                  [adzerk/boot-cljs-repl   "0.3.2"]
                  [com.cemerick/piggieback "0.2.1"  :scope "test"]
                  [weasel                  "0.7.0"  :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  ; project deps
                  [org.clojure/clojurescript "1.9.225"]
                  [play-cljs "0.6.4"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask run []
  (comp
    (serve :dir "target/public")
    (watch)
    (cljs-repl)
    (reload)
    (cljs :source-map true :optimizations :none)
    (target)))

(deftask build []
  (comp (cljs :optimizations :simple) (target)))

