(ns dev.user
  "This file should better be `dev/user.clj` but for whatever reason I
  can't get IntelliJ to properly indent it. As a result, putting it in
  `dev/dev/user`. My guess is some regression in IntelliJ 2021.1."
  (:require [clojure.data.csv :as csv]
            [jackdaw.client :as jc]
            [malli.generator :as mg]
            [piotr-yuxuan.tsv-processing.main :as main])
  (:import (java.lang AutoCloseable)
           (java.io StringWriter)))

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
        (Thread/sleep 1000) ; Trying to be nice with my machine.
        (let [k (mg/generate string?)
              writer (doto (StringWriter.)
                       (csv/write-csv
                         [(mg/generate [:vector
                                        [:or
                                         string?
                                         ;; So that we have some winners.
                                         [:enum "a" "b" "c" "d" "e" "f"]]])]
                         :separator \tab))
              v (str writer)]
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
