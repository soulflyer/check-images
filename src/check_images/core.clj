(ns check-images.core
  (:require [clojure.tools.cli :refer :all]
            [monger.collection :as mc]
            [monger.core :as mg]
            [image-lib.helper :refer [project-name]]
            [image-lib.file :as ilf]
            [image-lib.images :as ili])
  (:gen-class))

(def cli-options
  [["-d" "--database DATABASE" "specifies database to use"
    :default "photos"]
   ["-i" "--image-collection IMAGE-COLLECTION" "specifies the image collection"
    :default "images"]
   ["-c" "--count"    "returns the number of images not found"]
   ["-t" "--total"    "returns the total number of images in the database collection"]
   ["-s" "--summary"  "returns a list of projects containing missing images"]
   ["-x" "--lax"      "Matches any files with the same basename"]
   ["-X" "--very-lax" "Matches any file that starts with the same string"]
   ["-h" "--help"]
   ["-H" "--host HOST" "sets the db host" :default "localhost"]
   ["-p" "--port PORT" "sets the db port" :default 27017]])


(defn -main
  "Given a directory, ie Pictures/thumbs , checks if all the pics in the image collection
  are present in that directory"
  [& args]
  (let [{:keys [options arguments summary]} (parse-opts args cli-options)
        db (mg/get-db (mg/connect {:host (:host options) :port (:port options)}) (:database options))
        im (:image-collection options)
        all-images (ili/all-image-paths db im)
        picture-directory (first arguments)
        find-function (cond
                        (:lax options)
                        ilf/related-file-exists?
                        (:very-lax options)
                        ilf/loosely-related-file-exists?
                        :else
                        ilf/file-exists?)]
    (cond
      (:total options)
      (println (count (mc/find-maps db im)))
      (:count options)
      (println (count (ilf/missing-files all-images picture-directory find-function)))
      (:summary options)
      (doall
       (map
        println
        (sort (set (map project-name (ilf/missing-files all-images picture-directory find-function))))))
      (:help options)
      (println (str "Usage:\ncheck-images [options] path\ncheck-images -t\n\noptions:\n" summary))
      :else
      (doall
        (map println (ilf/missing-files all-images picture-directory find-function))))))

(comment
  (count (mc/find-maps (mg/get-db (mg/connect {:host "subversion.local"}) "photos") "images"))
  (count (mc/find-maps (mg/get-db (mg/connect {:host "localhost"}) "photos") "images"))
  (ili/all-image-paths (mg/get-db (mg/connect {:host "localhost"}) "photos") "images")
  (ilf/missing-files (ili/all-image-paths (mg/get-db (mg/connect {:host "localhost"}) "photos") "images") "/Users/iain/Pictures/Published/small" ilf/file-exists?)
  (-main "-Hlocalhost" "-t")
  (-main "-Hsubversion.local" "-t")
  )
