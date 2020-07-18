(ns skiverse.util.network)

(defn local-addresses []
  (->> (java.net.NetworkInterface/getNetworkInterfaces)
       enumeration-seq
       (map bean)
       (filter (complement :loopback))
       (mapcat :interfaceAddresses)
       (map #(.. % (getAddress) (getHostAddress)))))

(defn machine-name []
  (.getHostName (java.net.InetAddress/getLocalHost)))
