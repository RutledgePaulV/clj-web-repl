(ns clj-web-repl.core-test
  (:require [clojure.test :refer :all]
            [ring.adapter.jetty :as jetty]
            [clj-web-repl.core :refer :all]))


(jetty/run-jetty
  (wrap-repl-console
    (fn [request]
      (serve-page request))
    {:root "/admin"})
  {:port 3000})
