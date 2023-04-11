(defproject check-images "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.214"]
                 [com.novemberain/monger "3.6.0"]
                 [image-lib "0.2.6"]
                 [org.slf4j/slf4j-nop "2.0.7"]]
  :plugins [[lein-bin "0.3.4"]]
  :main ^:skip-aot check-images.core
  :profiles {:uberjar {:aot :all}}
  :bin {:name "check-images"
        :bin-path "~/bin"})
