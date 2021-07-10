(ns piotr-yuxuan.tsv-processing.user
  "This file should better be `dev/user.clj` but for whatever reason I
  can't get IntelliJ to properly indent it."
  (:require [piotr-yuxuan.tsv-processing.main :as main]
            [malli.generator :as mg]
            [jackdaw.client :as jc])
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

(defn producer
  []
  (let [producer-config {"bootstrap.servers" "localhost:9092"}
        topic-config (:input-topic main/default-config)]
    (with-open [client (jc/producer producer-config topic-config)]
      (dotimes [_ 10e3]
        (Thread/sleep 100)
        (let [k (mg/generate string?)
              v (mg/generate keyword?)]
          @(jc/produce! client topic-config k v))))))

(defonce producer-thread (atom nil))

(comment
  (reset! producer-thread
          (Thread. ^Runnable
                   (fn []
                     (println :starting)
                     (producer)
                     (println :done))))
  (.isAlive ^Thread @producer-thread)
  (.start ^Thread @producer-thread)
  (.isAlive ^Thread @producer-thread)
  (.stop ^Thread @producer-thread)
  (.isAlive ^Thread @producer-thread))
