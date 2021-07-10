(ns piotr-yuxuan.tsv-processing.main
  (:require [com.brunobonacci.mulog :as u]
            [jackdaw.streams :as j]
            [malli.core :as m]
            [malli.transform :as mt]
            [piotr-yuxuan.closeable-map :as closeable-map :refer [close-with with-tag closeable* closeable-map*]]
            [piotr-yuxuan.malli-cli :as malli-cli]
            [clojure.string :as str]
            [clojure.java.io :as io]))

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
               (u/trace ::mapper
                 [:k (pr-str k)
                  :v (pr-str v)]
                 [k "✨"])))
      (j/to output-topic)))

(defn kafka-streams
  [{:keys [streams-config input-topic output-topic]}]
  (u/log ::kafka-streams)
  (j/kafka-streams
    (doto (j/streams-builder)
      (topology input-topic
                output-topic))
    streams-config))

(defn env
  "Return the current environment the system is running in."
  []
  (or (System/getenv "ENV") "local"))

(def app-name
  "tsv-processing")

(defn version
  "Returns the version of the current version of the project from the resources."
  []
  (-> (io/resource (format "%s.version" app-name))
      slurp
      str/trim))

(defn start
  [config]
  (u/set-global-context! {:app-name app-name,
                          :version (version),
                          :env (env)})
  (closeable-map*
    (assoc config
      :log-publisher (closeable*
                       (with-tag ::closeable-map/fn (u/start-publisher! (:loggers config))))
      :streaming-app (closeable*
                       (doto (kafka-streams config)
                         j/start)))))

(def default-config
  {:streams-config {"application.id" "local.tsv-processing"
                    "bootstrap.servers" "localhost:9092"
                    "default.key.serde" "jackdaw.serdes.EdnSerde"
                    "default.value.serde" "jackdaw.serdes.EdnSerde"
                    "cache.max.bytes.buffering" 0
                    "auto.offset.reset" "latest"}
   :input-topic {:topic-name "local.tsv-processing.tsv-line.edn"
                 :partition-count 3
                 :replication-factor 1}
   :output-topic {:topic-name "local.tsv-processing.output.edn"
                  :partition-count 3
                  :replication-factor 1}
   :loggers {:type :multi
             :publishers
             [#_{:type :simple-file
                 :filename "target/events.edn"}
              {:type :jvm-metrics
               :sampling-interval 1000}
              {:type :prometheus
               :push-gateway {:job app-name
                              :endpoint "http://localhost:9091"}}
              {:type :console
               :pretty? true
               :transform (fn [events]
                            (remove (comp #{:mulog/jvm-metrics-sampled}
                                          :mulog/event-name)
                                    events))}]}})

(def Config
  (m/schema
    [:map {:decode/cli-args-transformer malli-cli/cli-args-transformer
           :closed true}
     [:streams-config
      [:map {:closed false}
       ["application.id" :string]
       ["bootstrap.servers" :string]
       ["default.key.serde" :string]
       ["default.value.serde" :string]
       ["cache.max.bytes.buffering" int?]
       ["auto.offset.reset" [:enum "latest" "earliest"]]]]
     [:input-topic [:map-of keyword? any?]]
     [:output-topic [:map-of keyword? any?]]
     [:loggers [:map {:closed false}
                [:type keyword?]]]]))

(defn load-config
  [args]
  (deep-merge default-config
              (m/decode Config args (mt/transformer malli-cli/cli-args-transformer
                                                    mt/strip-extra-keys-transformer))))

(defn -main
  [& args]
  (let [config (load-config args)]
    (assert (m/validate Config config))
    (start config)))

