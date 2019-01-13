(ns {{name}}.core (:require 
 [reitit.frontend :as reitit]))

(defn ^:export greet [] (js/alert "Howdy!"))
