(ns skiverse.util.hash
  (:import [java.security MessageDigest]))

(defn md5 [^String token]
  (let [hash-bytes
        (doto (^MessageDigest MessageDigest/getInstance "MD5")
          (.reset)
          (.update (.getBytes token)))
        hash-str
        (.toString
          (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
          16)
        hash-head
        (apply str (repeat (- 32 (count hash-str)) "0"))]
    (str hash-head hash-str)))
