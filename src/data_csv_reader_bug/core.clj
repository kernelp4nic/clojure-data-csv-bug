(ns data-csv-reader-bug.core
  (:require [clojure.data.csv :as csv]
            [data-csv-reader-bug.csv :as toolbox-csv]
            [clojure.java.io :as io]))

(defn -main
  [& args]
  (println "Starting process..")
  
  (io/delete-file "out/reader.csv" true)
  (io/delete-file "out/slurp.csv" true)
  (io/delete-file "out/csv-toolbox.csv" true)
  (io/delete-file "out/data-csv.csv" true)
  
  ;; slurp OK
  (spit "out/slurp.csv" (slurp "in.csv"))
  
  ;; reader & line-seq OK
  (with-open [reader (io/reader (io/file "in.csv"))]
    (doseq [row (line-seq reader)]
      (spit "out/reader.csv" (str row "\n") :append true)))
  
  ;; toolbox.csv
  (with-open [reader (io/reader (io/file "in.csv"))]
    (let [rows (line-seq reader)
          sep (toolbox-csv/guess-separator rows)]
      (doseq [row rows]
        (let [cells (toolbox-csv/csv-line row sep)]
          (spit "out/csv-toolbox.csv" (str (clojure.string/join sep cells) "\n") :append true)))))
  
  ;; broken
  (with-open [reader (io/reader (io/file "in.csv"))]
    (let [read-data (csv/read-csv reader :separator \;)]
      (with-open [writer (io/writer "out/data-csv.csv")]
        (csv/write-csv writer read-data :separator \;))))
  
  (println "Finished.")
  (System/exit 0))
