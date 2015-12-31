(ns check-images.core
  (:require [clojure.tools.cli :refer :all]
            [monger.collection :as mc]
            [monger.core :as mg]
            [image-lib.core    :refer [find-images
                                       find-images-containing
                                       find-sub-keywords
                                       image-path
                                       image-paths
                                       basename
                                       project-name
                                       file-exists?
                                       related-file-exists?
                                       loosely-related-file-exists?
                                       missing-files]])
  (:gen-class))

(def cli-options
  [["-d" "--database DATABASE" "specifies database to use"
    :default "soulflyer"]
   ["-i" "--image-collection IMAGE-COLLECTION" "specifies the image collection"
    :default "images"]
   ["-c" "--count"    "returns the number of images not found"]
   ["-t" "--total"    "returns the total number of images in the database collection"]
   ["-s" "--summary"  "returns a list of projects containing missing images"]
   ["-x" "--lax"      "Matches any files with the same basename"]
   ["-X" "--very-lax" "Matches any file that starts with the same string"]
   ["-h" "--help"]])


(defn -main
  "Given a directory, ie Pictures/thumbs , checks if all the pics in the image collection
  are present in that directory"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        connection (mg/connect)
        db (mg/get-db connection (:database options))
        im (:image-collection options)
        find-function (cond
                        (:lax options)
                        related-file-exists?
                        (:very-lax options)
                        loosely-related-file-exists?
                        :else
                        file-exists?)]

    (cond
      (:total options)
      (println (count (mc/find-maps db im)))
      (:count options)
      (println (count (missing-files db im (first arguments) find-function)))
      (:summary options)
      (doall
       (map
        println
        (sort (set (map project-name (missing-files db im (first arguments) find-function))))))
      (:help options)
      (println (str "Usage:\ncheck-images [options] path\ncheck-images -t\n\noptions:\n" summary))
      :else
      (doall
       (map println (missing-files db im (first arguments) find-function))))))
