(defproject check-images "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [com.novemberain/monger "3.0.1"]
                 [image-lib "0.1.0-SNAPSHOT"]
                 [org.slf4j/slf4j-nop "1.7.12"]]
  :plugins [[lein-bin "0.3.4"]]
  :main ^:skip-aot check-images.core
  :profiles {:uberjar {:aot :all}}
  :bin {:name "check-images"
        :bin-path "~/bin"})
