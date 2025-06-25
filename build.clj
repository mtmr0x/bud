(ns build
  (:require
    [clojure.tools.build.api :as b]))

(def lib 'org.clojars.mat/bud)
(def version "0.2.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn build-pom [_]
  (b/write-pom {:basis     basis
                :lib       lib
                :version   version
                :target    ""}))

(defn jar [_]
  (b/copy-dir {:src-dirs   ["src"]
               :target-dir class-dir})
  (b/write-pom {:basis     basis
                :lib       lib
                :version   version
                :target    ""})
  (b/jar {:class-dir class-dir
          :jar-file  "target/bud.jar"}))
