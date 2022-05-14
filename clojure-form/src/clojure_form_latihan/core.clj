(ns clojure-form-latihan.core
  (:require [clojure.java.io :as io]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [ring.util.response :as ring-resp]))

(defn html-response
  [html]
  {:status 200 :body html :headers {"Content-Type" "text/html"}})

(defn intro-form
  [req]
  (html-response
    (slurp (io/resource "form.html"))))

(def store (atom {}))

(defn list-data [request]
  {:status 200 :body @store})

(defn input-data
  [req]
  (let [uuid (java.util.UUID/randomUUID)
        name (get-in req [:params :name])
        club (get-in req [:params :club])
        nationality (get-in req [:params :nationality])]
    (swap! store assoc uuid {:name name :club club :nationality nationality})
    {:status 200 :headers {"Content-Type" "text/html"} :body "<html><body>Data Saved. <br> <a href=\"/\"><u>Input more</u></a> <br>
    <a href=\"/save\"><u>Download Data</u></a></body></html>"}))

(def routes
    (route/expand-routes
      [[["/" {:get `intro-form}]
        ["/input" ^:interceptors [(body-params/body-params)
                                  middlewares/params
                                  middlewares/keyword-params]
         {:post `input-data}]
        ["/save" {:get 'list-data}]]]))

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
