(defproject ssas-driver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
  [org.olap4j/olap4j "1.2.0"]
[org.clojure/java.jdbc "0.7.11"]
  ]
  :repl-options {:init-ns ssas-driver.core}
  :uberjar-name "ssas-driver-0.1.0-SNAPSHOT-standalone.jar"
  :manifest {"Metabase-Version" "1.0.0"
             "Metabase-Database" "SSAS"
             "Metabase-Driver-Name" "My SSAS Driver"
             "Metabase-Driver-Classpath" "/plugins/ssas-driver-0.1.0-SNAPSHOT-standalone.jar"})
