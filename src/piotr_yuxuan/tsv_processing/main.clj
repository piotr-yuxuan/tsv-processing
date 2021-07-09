(ns piotr-yuxuan.tsv-processing.main
  (:require [clojure.data.csv :as csv]
            [piotr-yuxuan.malli-cli :as malli-cli]
            [piotr-yuxuan.closeable-map :refer [close-with with-tag closeable* closeable-map*]]
            [jackdaw.streams :as j]
            [malli.core :as m]
            [clojure.pprint :as pp]
            [malli.transform :as mt])
  (:import (java.lang AutoCloseable)))

(defn deep-merge
  "Like merge, but merges maps recursively. It merges the maps from left
  to right and the right-most value wins. It is useful to merge the
  user defined configuration on top of the default configuration.

  See original [source](https://github.com/BrunoBonacci/1config/blob/4cf8284b1b490253ac617bec9d2348c3234931e6/1config-core/src/com/brunobonacci/oneconfig/util.clj#L23)."
  [& maps]
  (let [maps (filter (comp not nil?) maps)]
    (if (every? map? maps)
      (apply merge-with deep-merge maps)
      (last maps))))

(defn topology
  [streams-builder input-topic output-topic]
  (-> (j/kstream streams-builder input-topic)
      (j/map (fn mapper [[k v]]
               (println ::mapper
                        :k (pr-str k)
                        :v (pr-str v))
               [k "✨"]))
      (j/to output-topic)))

(defn kafka-streams
  [{:keys [streams-config input-topic output-topic]}]
  (j/kafka-streams
    (doto (j/streams-builder)
      (topology input-topic
                output-topic))
    streams-config))

(defn start
  [config]
  (closeable-map*
    (assoc config
      :streaming-app (closeable* (doto (kafka-streams config)
                                   j/start)))))

(def Config
  (m/schema
    [:map
     [:streams-config
      [:map-of {:default {"application.id" "local.tsv-processing"
                          "bootstrap.servers" "localhost:9092"
                          "default.key.serde" "jackdaw.serdes.EdnSerde"
                          "default.value.serde" "jackdaw.serdes.EdnSerde"
                          "cache.max.bytes.buffering" "0"}}
       string? string?]]
     [:input-topic
      [:map-of {:default {:topic-name "local.tsv-processing.tsv-line.edn"
                          :partition-count 3
                          :replication-factor 1}}
       keyword? any?]]
     [:output-topic
      [:map-of {:default {:topic-name "local.tsv-processing.output.edn"
                          :partition-count 3
                          :replication-factor 1}}
       keyword? any?]]]))

(defn load-config
  [args]
  (deep-merge (m/decode Config {} mt/default-value-transformer)
              (m/decode Config args (mt/transformer malli-cli/cli-args-transformer))))

(defn -main
  [& args]
  (let [config (load-config args)]
    (if (m/validate Config config)
      (start config)
      (pp/pprint (m/explain Config config)))))

(defonce app
  (atom nil))

(defn user-start
  []
  (when-not @app
    (reset! app (-main))))

(defn user-stop
  []
  (when @app
    (.close ^AutoCloseable (-main))
    (reset! app nil)))

(comment
  ;; Start with:
  (user-start)
  ;; Restart with:
  (do (user-stop)
      (user-start)))
