(ns fops.document
  (:use clojure.java.io)
  (:import
   (java.io.File)
   (org.apache.fop.apps FopFactory Fop MimeConstants)
   (javax.xml.transform TransformerFactory)
   (javax.xml.transform.sax SAXResult)
   (javax.xml.transform.stream StreamSource)))

(def fopFactory (FopFactory/newInstance))
(def FORMATS {:rtf MimeConstants/MIME_RTF
              :tiff MimeConstants/MIME_TIFF
              :pdf MimeConstants/MIME_PDF
              :pcl MimeConstants/MIME_PCL
              :png MimeConstants/MIME_PNG
              :ps MimeConstants/MIME_POSTSCRIPT
              :txt MimeConstants/MIME_PLAIN_TEXT})

(defn transform-fo [fo-input-stream doc-output-stream mime]
  (let [fop (.newFop fopFactory mime doc-output-stream)
        factory (TransformerFactory/newInstance)
        transformer (.newTransformer factory)
        src (StreamSource. fo-input-stream)
        res (SAXResult. (.getDefaultHandler fop))]
    (.transform transformer src res)
    true))

(defn stream-doc [in-fop-stream ext]
  (if-let [mime ((keyword ext) FORMATS)]
    (let [temp-file (java.io.File/createTempFile "doc" (format ".%s" ext))]
      (with-open [out-file (output-stream temp-file)]
        (transform-fo in-fop-stream out-file mime)
        (input-stream temp-file)))))

(defn write-doc [in-fop-filepath out-filepath ext]
  (if-let [mime ((keyword ext) FORMATS)]
    (with-open [src-fop (input-stream in-fop-filepath)
                out-doc (output-stream out-filepath)]
      (transform-fo src-fop out-doc mime))))

(defn write-pdf [in-fop-filepath out-filepath]
  (write-doc in-fop-filepath out-filepath "pdf"))

(defn stream-pdf [in-fop-stream]
  (stream-doc in-fop-stream  "pdf"))
