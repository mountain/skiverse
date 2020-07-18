(ns skiverse.util.gzip
  (:import [java.io ByteArrayInputStream]
           [java.util.zip GZIPInputStream]))

(defn unzip ^String [^bytes byte-array]
  (let [^java.io.ByteArrayInputStream bis  (ByteArrayInputStream. byte-array)
        ^java.util.zip.GZIPInputStream gis (GZIPInputStream. bis)]
    (slurp gis)))
