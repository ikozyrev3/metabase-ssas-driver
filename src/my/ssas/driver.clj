(ns my.ssas.driver
  (:import (org.olap4j.metadata Cube Dimension Hierarchy Level Member Property)
           (org.olap4j OlapConnection OlapWrapper)
           (java.sql DriverManager))		
  (:require [clojure.string :as str]
            [clojure.java.jdbc :as jdbc]))

;; Connection spec creation function
(defn connection-details->spec [details]
  {:classname "my.ssas.driver.Driver"
   :subprotocol "my-ssas-driver"
   :subname (str "//" (:host details) ":" (:port details) "/" (:database details))
   :user (:username details)
   :password (:password details)})
   
(defn register []
  {:metabase.driver/name "My SSAS Driver"
   :metabase.driver/url  "jdbc:my-ssas-driver://localhost:1234/mydb"
   :metabase.driver/class-for-name "my.ssas.driver.Driver"
   :metabase.driver/protocol "my-ssas-driver"
   :metabase.driver/jdbc-requirements {:class-for-name "my.ssas.driver.Driver"
                                       :connection-details->spec :ssas}
   :jdbc-driver {:name "my-ssas-driver"
                 :class-name "my.ssas.driver.Driver"
                 :subprotocol "my-ssas-driver"
                 :subname "//localhost:1234/mydb"}})

(defn- to-camel-case [^String s]
  (-> s
      (str/replace #"[^A-Za-z0-9 ]" " ")
      (str/replace #"\s+" " ")
      (str/trim)
      (str/lower-case)))

(defn- to-table-name [^String s]
  (-> s
      (to-camel-case)
      (str/replace #"\s+" "_")
      (str/replace #"^[^A-Za-z]+" "")
      (str/replace #"[^A-Za-z0-9_]+" "_")))

(defn- create-cube-metadata [conn catalog cube-name]
  (let [cube-metadata (jdbc/query conn [(str "SELECT * FROM $system.MDSchema_Cubes WHERE CUBE_NAME='" cube-name "'")])]
    (when-let [c (first cube-metadata)]
      (let [dimensions-metadata (jdbc/query conn [(str "SELECT * FROM $system.MDSchema_Dimensions WHERE CUBE_NAME='" cube-name "'")])]
        {:name cube-name
         :dimensions (map (fn [d]
                            (let [name (to-table-name (:DIMENSION_NAME d))]
                              {:name name
                               :hierarchies (map (fn [h]
                                                   (let [hierarchy-name (to-table-name (:HIERARCHY_NAME h))]
                                                     {:name hierarchy-name
                                                      :levels (map (fn [l]
                                                                     (let [level-name (to-table-name (:LEVEL_NAME l))]
                                                                       {:name level-name
                                                                        :column (str (:CUBE_NAME c) "." hierarchy-name "." level-name)
                                                                        :type "Text"
                                                                        :dimension name}))
                                                                   (jdbc/query conn [(str "SELECT * FROM $system.MDSchema_Levels WHERE HIERARCHY_NAME='" (:HIERARCHY_NAME h) "' AND CUBE_NAME='" cube-name "'")]))}))
                                               (jdbc/query conn [(str "SELECT * FROM $system.MDSchema_Hierarchies WHERE DIMENSION_NAME='" (:DIMENSION_NAME d) "' AND CUBE_NAME='" cube-name "'")]))}))
                          dimensions-metadata)}))))

(defn create-metadata [conn catalog]
  (let [metadata {:cubes (map (fn [c]
                                (create-cube-metadata conn catalog (:CUBE_NAME c)))
                              (jdbc/query conn [(str "SELECT * FROM $system.MDSchema_Cubes WHERE CUBE_CAPTION='" catalog "'")]))}]
    (-> metadata
        (assoc :dimensions (flatten (map :dimensions (:cubes metadata)))))))

(defn ssas-driver [config]
  (let [url (-> config :url)
        catalog (-> config :catalog)
        conn (jdbc/get-connection url)]
    (try
      ; Note: XmlaOlap4jDriver registration removed due to dependency issues
      ; This would need to be properly configured with SSAS connection setup
      (create-metadata conn catalog)
    (catch Exception e
      (throw e))
    (finally
      (when conn (.close conn))))))
