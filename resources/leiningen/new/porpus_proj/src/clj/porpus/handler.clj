(ns {{name}}.handler
  (:require 
  	    [reitit.ring.coercion :as rrc]
            [reitit.coercion.spec]
	    [reitit.ring :as reitit-ring]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :as memory]
            [{{name}}.middleware :refer [middleware]]
            [hiccup.page :refer [include-js include-css html5]]
            [config.core :refer [env]]))

(def store (memory/memory-store))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [    
     ["/buttontest"
        {:get {
         :handler (fn [stuff]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)[:button {:onclick "{{sanitized}}.core.greet()"} "Say hi"]
                                   (include-js "/js/app.js"))})}}]
     ["/parmtest"
        {:get {
	       :coercion reitit.coercion.spec/coercion
               :parameters { :query  {:n int?} }
               :handler (fn [{ {n :n} :params }]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)
		     	   [:span (str "Parameter is " n)]
		        (include-js "/js/app.js"))})}}]
     ["/seshtest"
        {:get {
               :handler (fn [{session :session}]
                                   {:status 200 :headers {"Content-Type" "text/html"}
                                    :session (assoc session :markuse (inc (:markuse session 0)))
                                    :body (html5 (head)[:body [:span (:markuse session 0)
				    ]])})}}]
     ["/seshtest2"
        {:get {
               :handler (fn [{session :session}]
                                   {:status 200 :headers {"Content-Type" "text/html"}
                                    :session (assoc session :markuse (inc (:markuse session 0)))
                                    :body (html5 (head)[:body [:span (:markuse session 0) 
                                    ]])})}}]
     ["/formtest"
      {:get {:coercion reitit.coercion.spec/coercion             
             :handler (fn [requestobj]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)
			  [:form { :method "post" :action "/formtest"}
			   [:input {:name "username"}]
			   [:input {:type "submit" :value "OK"}]])})}
       :post {:parameters {:body {:username string?}}
              :handler (fn  [{ {u :username} :params session :session ip :remote-addr}]
               {:status 200
                :headers {"Content-Type" "text/html"}
                :body (html5 (head) [:span (str "Hi, " u " from " ip ".")])})}}]
    ]
    {:data {:middleware (concat [[wrap-session {:store store}]] middleware) }})
    (reitit-ring/routes
     (reitit-ring/create-resource-handler {:path "/" :root "/public"})
     (reitit-ring/create-default-handler))))
