(defproject metabase-ssas "0.1.0-SNAPSHOT"
  :description "Custom driver for connecting to an OLAP4J data source"
  :dependencies [
					[org.clojure/clojure "1.10.1"]
					[org.olap4j/olap4j "1.2.0"]
					[org.olap4j/olap4j-xmla "1.1.0"]
				 ]
  :repositories {"mvn"   
					{:url "https://www.mvnrepository.com"}
				"jfrog"
					{:url "https://jaspersoft.jfrog.io/artifactory/repo/"}
				
				"jboss"
					{:url "https://repository.jboss.org/"}
					}
)
