(ns check-images.core
  (:require [clojure.tools.cli :refer :all]
            [monger.collection :as mc]
            [monger.core :as mg]
            [image-lib.core    :refer [find-images
                                       find-images-containing
                                       find-sub-keywords
                                       image-path]])

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

(defn loosely-related-file-exists?
  "given a pathname to a file, checks if any variant of the file exists
  (loosely-related-file exists? /home/me/picture/abc.jpg
  will return true if any file exists in /home/me/pictures  that starts with abc
  ie: abc.jpg abc.png, abc-version2.jpg etc."
  [path]
  (let [file (java.io.File. path)
        dir  (.getParentFile file)
        files (.list dir)]
    (< 0 (count (filter #(re-find (re-pattern %) path) (map basename files))))))

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
