(ns {{name}}.middleware
    (:require 
              [reitit.ring.coercion :as rrc]
    	      [ring.middleware.content-type :refer [wrap-content-type]]
              [ring.middleware.params :refer [wrap-params]]
              [prone.middleware :refer [wrap-exceptions]]
              [ring.middleware.reload :refer [wrap-reload]]
              [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(def middleware
  [#(wrap-defaults % (assoc site-defaults :session false))
   wrap-exceptions
   wrap-reload

   rrc/coerce-exceptions-middleware
   rrc/coerce-request-middleware
   rrc/coerce-response-middleware



])
