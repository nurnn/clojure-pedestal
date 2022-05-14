(ns clojure-form-latihan.server
  (:gen-class) ; for -main method in uberjar
  (:require [io.pedestal.http :as server]
            [clojure-form-latihan.core :as service]))

(defonce runnable-service (server/create-server service/service))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (-> service/service
      (merge {:env :dev
              ::server/join? false
              ::server/routes #(deref #'service/routes)
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (server/start runnable-service))
