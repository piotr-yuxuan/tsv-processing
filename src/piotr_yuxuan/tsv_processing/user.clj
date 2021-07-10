(ns piotr-yuxuan.tsv-processing.user
  "This file should better be `dev/user.clj` but for whatever reason I
  can't get IntelliJ to properly indent it."
  (:require [piotr-yuxuan.tsv-processing.main :as main])
  (:import (java.lang AutoCloseable)))

(defonce app
  (atom nil))

(defn start
  []
  (when-not @app
    (println :starting)
    (reset! app (main/-main))))

(defn stop
  []
  (when @app
    (println :stopping)
    (.close ^AutoCloseable @app)
    (reset! app nil)))

(defn restart
  []
  (stop)
  (start))
