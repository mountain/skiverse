(defproject skiverse "0.0.0"
  :description "A SKI universe"
  :url "http://example.com/FIXME"
  :license {:name "CC0 1.0 Universal"
            :url "https://creativecommons.org/publicdomain/zero/1.0/"}

  :repositories [["bintray" "https://jcenter.bintray.com/"]]

  :dependencies [[org.clojure/clojure "1.10.1"]

                 [net.mikera/vectorz "0.67.0"]

                 [org.clojure/tools.logging "1.0.0"]
                 [org.slf4j/slf4j-api "1.7.30"]
                 [log4j/log4j "1.2.17"]
                 [org.apache.logging.log4j/log4j "2.13.1" :extension "pom"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.13.1"]

                 [http-kit "2.4.0-alpha6"]
                 [compojure "1.6.1"]

                 [ring/ring-core "1.8.0"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-codec "1.1.2"]
                 [ring/ring-headers "0.3.0"]
                 [ring/ring-json "0.5.0"]
                 [ring.middleware.jsonp "0.1.6"]

                 [org.mapdb/mapdb "3.0.8"]

                 [org.yaml/snakeyaml "1.25"]
                 [clj-pid "0.1.2"]
                 [clj-sub-command "0.6.0"]

                 [org.junit.jupiter/junit-jupiter "5.4.2" :scope "tests"]]

  :jvm-opts ["-Xms1g" "-Xmx5g" "-server"]
  :javac-options ["-target" "1.8" "-source" "1.14" "-Xlint:-options"]
  :omit-source true

  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["src/main/resources"]
  :test-paths ["src/tests/java/"]
  :test-selectors {:default (complement :integration)
                   :integration :integration
                   :all (constantly true)}

  :jar-name "skiverse.jar"
  :uberjar-name "skiverse-standalone.jar"

  :aot :all
  :main skiverse.main)
