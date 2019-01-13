(ns ^:figwheel-no-load {{name}}.dev
  (:require
    [{{name}}.core :as core]
    [devtools.core :as devtools]))

(devtools/install!)

(enable-console-print!)

