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
   ["-c" "--count" "returns the number of images not found"]
   ["-t" "--total" "returns the total number of images in the database collection"]
   ["-s" "--summary" "returns a list of projects containing missing images"]
   ["-x" "--lax" "Searches for any files with the same basename"]
   ["-h" "--help"]])

;; (defn image-paths
;;   [database image-collection]
;;   (let [connection (mg/connect)
;;         db (mg/get-db connection database)]
;;     (map image-path (mc/find-maps db image-collection {}))))

(defn image-paths
  [db image-collection]
  (map image-path (mc/find-maps db image-collection {})))

(defn basename
  "Cuts the extension off the end of a string"
  [filename]
  (let [index-dot (.lastIndexOf filename ".")
        index-slash (+ 1 (.lastIndexOf filename "/"))]
    (if (< 0 index-dot)
      (subs filename index-slash index-dot)
      filename)))

(defn project-name
  [filename]
  (let [index-slash (.lastIndexOf filename "/")]
    (if (< 0 index-slash)
      (subs filename 0 index-slash)
      filename)))

(defn file-exists?
  [path]
  (let [file (java.io.File. path)]
    (.exists file)))

(defn related-file-exists?
  [path]
  (let [file (java.io.File. path)
        dir  (.getParentFile file)
        files (.list dir)]
    (if (some #{(basename path)} (seq (map basename files))) true false)))

(defn missing-files
  [database image-collection root-path find-function]
  (remove (fn [im] (find-function (str root-path "/" im))) (image-paths database image-collection))
  )

(defn -main
  "Given a directory, ie Pictures/thumbs , checks if all the pics in the image collection
  are present in that directory"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        connection (mg/connect)
        db (mg/get-db connection (:database options))
        im (:image-collection options)
        find-function (if (:lax options) related-file-exists? file-exists?)]

    (cond
      (:total options)
      (count (mc/find-maps db im))
      (:count options)
      (count (missing-files db im (first arguments) find-function))
      (:summary options)
      (set (map project-name (missing-files db im (first arguments) find-function)))
      (:help options)
      (println (str "Usage:\ncheck-images [options] keyword\n\noptions:\n" summary))
      :else
      (missing-files db im (first arguments) find-function))))
