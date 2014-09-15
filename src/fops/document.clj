(ns fops.document
  (:use clojure.java.io)
  (:import
   (java.io.File)
   (org.apache.fop.apps FopFactory Fop MimeConstants)
   (javax.xml.transform TransformerFactory)
   (javax.xml.transform.sax SAXResult)
   (javax.xml.transform.stream StreamSource)))

(def fopFactory (FopFactory/newInstance))


(defn transform-fo [fo-input-stream pdf-output-stream]
  (let [fop (.newFop fopFactory MimeConstants/MIME_PDF pdf-output-stream)
        factory (TransformerFactory/newInstance)
        transformer (.newTransformer factory)
        src (StreamSource. fo-input-stream)
        res (SAXResult. (.getDefaultHandler fop))]
    (.transform transformer src res)))

(defn stream-pdf [in-fop-stream]
  (let [temp-pdf (java.io.File/createTempFile "doc" ".pdf")]
    (with-open [out-pdf (output-stream temp-pdf)]
      (transform-fo in-fop-stream out-pdf))
    (input-stream temp-pdf)))



(defn write-pdf [in-fop-filepath out-filepath]
  (with-open [src-fop (file in-fop-filepath)
              out-pdf (output-stream out-filepath)]
    (transform-fo src-fop out-pdf)))
