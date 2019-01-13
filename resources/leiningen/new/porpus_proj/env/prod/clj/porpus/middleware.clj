(ns {{name}}.middleware
    (:require 
              [reitit.ring.coercion :as rrc]
    	      [ring.middleware.content-type :refer [wrap-content-type]]
              [ring.middleware.params :refer [wrap-params]]
              [ring.middleware.reload :refer [wrap-reload]]
              [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(def middleware
   [#(wrap-defaults % (assoc-in (assoc site-defaults :session false ) [:security :anti-forgery] false ))
   rrc/coerce-exceptions-middleware
   rrc/coerce-request-middleware
   rrc/coerce-response-middleware

])



