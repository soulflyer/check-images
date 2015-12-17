(ns check-images.core
  (:require [clojure.tools.cli :refer :all]
            [monger.collection :as mc]
            [monger.core :as mg])
  (:use     [find-images.core :exclude [-main cli-options]])
  (:gen-class))

(def cli-options
  [["-d" "--database DATABASE" "specifies database to use"
    :default "soulflyer"]
   ["-i" "--image-collection IMAGE-COLLECTION" "specifies the image collection"
    :default "images"]
   ["-h" "--help"]])

(defn image-paths
  [database image-collection]
  (let [connection (mg/connect)
        db (mg/get-db connection database)]
    (map image-path (mc/find-maps db image-collection {}))))

(defn check-file
  [path]
  (let [file (java.io.File. path)]
    (.exists file)))

(defn check-files
  [database image-collection root-path]
  (remove (fn [im] (check-file (str root-path "/" im))) (image-paths database image-collection))
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        connection (mg/connect)
        database (mg/get-db connection (:database options))]

    (cond
      (:help options)
      (println (str "Usage:\ncheck-images [options] keyword\n\noptions:\n" summary))
      :else
      (println "Hello, World!"))))
