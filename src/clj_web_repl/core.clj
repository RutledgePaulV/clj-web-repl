(ns clj-web-repl.core
  (:require [cheshire.core :as cheshire]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import (java.io File StringWriter)))

(defn resource [resource]
  (slurp (io/input-stream (io/resource (str "clj_web_repl/" resource)))))

(defn replace [page [k v]]
  (string/replace page (str "{{" (name k) "}}") v))

(defn template [page context]
  (reduce replace page context))

(defn response [body]
  {:body body :status 200})

(defn content-type [response type]
  (assoc-in response [:headers "content-type"] type))

(defn serve-javascript [file]
  (->
    (resource file)
    (response)
    (content-type "application/javascript")))

(defn serve-page [root file]
  (->
    (resource file)
    (template
      {:root           root
       :main           (str root "/main.js")
       :styles         (str root "/styles.css")
       :jquery         (str root "/jquery.min.js")
       :codemirror     (str root "/codemirror.min.js")
       :jquery.console (str root "/jquery.console.min.js")})
    (response)
    (content-type "text/html")))

(defn serve-css [file]
  (->
    (resource file)
    (response)
    (content-type "text/css")))

(defn serve-json [body]
  (->
    body
    (cheshire/generate-string)
    (response)
    (content-type "application/json")))

(alter-var-root #'serve-javascript memoize)
(alter-var-root #'serve-page memoize)
(alter-var-root #'serve-css memoize)
(alter-var-root #'resource memoize)

(defn get-extension [filename]
  (first (re-find #"(\.[^.]*)$" filename)))

(defn get-files []
  (->>
    (file-seq (io/file (io/resource "clj_web_repl")))
    (filter #(.isFile %))
    (map #(.getName ^File %))))

(defn get? [req]
  (= :get (:request-method req)))

(defn post? [req]
  (= :post (:request-method req)))

(defn make-mappings [root]
  (into {} (map #(vector (str root "/" %) %) (get-files))))

(defn serving-fn-by-ext [root]
  (fn [filename]
    (get
      {".css"  (serve-css filename)
       ".js"   (serve-javascript filename)
       ".html" (serve-page root filename)}
      (get-extension filename))))

(defn process [expr]
  (with-open [out (StringWriter.)]
    (let [result (eval (binding [*read-eval* false] (read-string expr)))]
      {:expr expr :result (str out (pr-str result))})))

(defn execute [req]
  (let [body (slurp (:body req))
        {:keys [expr]} (cheshire/parse-string body true)]
    (process expr)))

(defn wrap-repl-console [handler {:keys [root]}]
  (let [mappings (make-mappings root)]
    (fn [request]
      (or

        (and (post? request)
          (when (= (str root "/exec") (:uri request))
            (serve-json (execute request))))

        (and (get? request)
          (when-some [file-to-serve (mappings (:uri request))]
            ((serving-fn-by-ext root) file-to-serve)))

        (handler request)))))
