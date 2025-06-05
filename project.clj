(defproject metabase-ssas "0.1.0-SNAPSHOT"
  :description "Custom driver for connecting to an OLAP4J data source"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.olap4j/olap4j "1.2.0"]
                 [org.clojure/java.jdbc "0.7.12"]]
  :repositories [["central" {:url "https://repo1.maven.org/maven2/"}]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :uberjar-name "ssas-driver.jar")
