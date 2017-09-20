(ns data-csv-reader-bug.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(def separators ["," ";" "\\\\|" "\t" "~" "^"])
(def patterns
  (->> separators
       (reduce #(assoc %1 %2 (re-pattern
                               (clojure.string/replace "$sep\\s*\"([^\"]*)\"|$sep\\s*([^\"$sep]*)"
                                                       #"\$sep" %2)))
               {})))

(defn- split-line
  "Splits a line using the specified separator.

   Returns the complete re-seq result so no post-filtering
   on the regular expression match is done here."
  [line separator]
  (->> (str separator line)
       (re-seq (get patterns separator))))

(defn- count-fields
  "Counts how many fields a given line has using
   the specified separator"
  [line separator]
  (-> (split-line line separator)
      count))

(defn guess-separator
  "Given a file line sequence guess the best separator to use for the file
   using the first 20 rows of it as sample."
  [fseq]
  (let []
    (->> (take 20 fseq)
         (map #(zipmap separators (map (partial count-fields %) separators)))
         (apply merge-with +)
         (apply max-key val)
         key)))

(defn csv-line
  "Transforms a text line into a csv sequence"
  [line separator]
  (mapv (comp first rest (partial filter identity))
        (split-line line separator)))
