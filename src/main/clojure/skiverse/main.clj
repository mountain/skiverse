(ns skiverse.main
  (:import skiverse.ParticleSystem)
  (:require [clojure.tools.logging :as log])
  (:gen-class))

(defn -main []
  (ParticleSystem/main (make-array java.lang.String 0)))
