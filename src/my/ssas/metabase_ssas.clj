(ns metabase_ssas
  (:import [org.olap4j.OlapConnection]
           [org.olap4j.xmla.XmlaOlap4jDriver]
           [java.sql.DriverManager])
  (:require [metabase.db.metadata :as metadata]))

(defn make-olap-connection [params]
  (let [url (str "jdbc:xmla:Server=" (:server params)
                  ";Database=" (:database params)
                  ";User=" (:user params)
                  ";Password=" (:password params)
                  ";Provider=" (:provider params))]
    (-> (new XmlaOlap4jDriver)
        (.connect url {}))))

(metadata/register-driver
  {:name "SSAS"
   :class-for-name  {:class metabase_ssas/make-olap-connection
                    :subprotocol "olap4j"
                    :subname "xmla:Server=myServerName;Catalog=myDB"}
   :supports-sql? false})
