(ns latihan.core
  (:require [io.pedestal.http.route :as route]
            [io.pedestal.http :as http]
            [io.pedestal.test :as test]
            [latihan.database :as database]))

(defn assoc-store [context]
  (update context :request assoc :store database/store))

(def db-interceptor
  {:nama :db-interceptor
   :enter assoc-store})

(defn list-data [req]
  {:status 200 :body @(:store req)})

(defn map-data [uuid nama klub negara]
  {:id uuid :nama nama :klub klub :negara negara})

(defn input-data [req]
  (let [uuid (java.util.UUID/randomUUID)
        nama (get-in req [:query-params :name])
        klub (get-in req [:query-params :klub])
        negara (get-in req [:query-params :negara])
        data (map-data uuid nama klub negara)
        store (:store req)]
    (swap! store assoc uuid data)
    {:status 200 :body data}))

(def routes (route/expand-routes
              #{["/todo" :post [db-interceptor input-data] :route-name :input-data]
                ["/todo" :get [db-interceptor list-data] :route-name :list-data]}))

(def service-map {::http/routes routes
                  ::http/port   9999
                  ::http/type   :jetty
                  ::http/join?  false})

(def server (atom nil))

(defn start-server []
  (reset! server (http/start (http/create-server service-map))))

(defn test-request [verb url]
  (test/response-for (::http/service-fn @server) verb url))

(start-server)

;;contoh-input-data
;(test-request :post "/todo?name=Salah&klub=Liverpool&negara=Mesir")
;(test-request :post "/todo?name=Mane&klub=Liverpool&negara=Senegal")

;;contoh-save-data
;(clojure.edn/read-string (:body (test-request :get "/todo")))
