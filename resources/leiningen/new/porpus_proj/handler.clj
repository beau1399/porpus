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

(def mount-target
  [:div#app
   [:h2 "Welcome to {{name}}"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exсiting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    [:button {:onclick "{{name}}.core.greet()"} "Say hi"]
    (include-js "/js/app.js")]))

(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     

     ["/buttontest"
        {:get {
         :handler (fn [stuff]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)[:button {:onclick "{{name}}.core.greet()"} "Say hi"]  (include-js "/js/app.js"))})}}]

     ["/parmtest"
        {:get {
	       :coercion reitit.coercion.spec/coercion
               :parameters { :query  {:n int?} }
               :handler (fn [{ {n :n} :params }]
                    {:status 200
                     :headers {"Content-Type" "text/html"}
                     :body (html5 (head)
		     	   [:span (str "Parameter is" n)]
		        (include-js "/js/app.js"))})}}]

       ["/seshtest"
        {:get {
               :handler (fn [{session :session}]
                                   {:status 200 :headers {"Content-Type" "text/html"}
                                    :session (assoc session :markuse (inc (:markuse session 400)) ); :cookies {:ring-session 1}
                                    :body (html5 (head)[:body [:span (keys session)] [:span (str (:markuse session "~") (:authuser session "au") "!"
				    )]])})}}]

       ["/seshtest2"
        {:get {
               :handler (fn [{session :session}]
                                   {:status 200 :headers {"Content-Type" "text/html"}
                                    :session (assoc session :markuse (inc (:markuse session 400)) )
                                    :body (html5 (head)[:body [:span (keys session)] [:span (str (:markuse session "~") (:authuser session "au") "!"
                                    )]])})}}]

     


     ["/items"
      ["" {:get {:handler index-handler}}]
      ["/:item-id" {:get {:handler index-handler
                          :parameters {:path {:item-id int?}}}}]]
     ["/about" {:get {:handler index-handler}}]]
     {:data {:middleware (concat [[wrap-session {:store store}]] middleware) }})
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))))
