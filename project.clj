(defproject com.github.piotr-yuxuan/tsv-processing (-> "./resources/tsv-processing.version" slurp .trim)
  :description ""
  :url "https://github.com/piotr-yuxuan/tsv-processing"
  :license {:name "European Union Public License 1.2 or later"
            :url "https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12"
            :distribution :repo}
  :scm {:name "git"
        :url "https://github.com/piotr-yuxuan/tsv-processing"}
  :pom-addition [:developers [:developer
                              [:name "胡雨軒 Петр"]
                              [:url "https://github.com/piotr-yuxuan"]]]
  :dependencies [;; Core constructs
                 [org.clojure/clojure "1.11.1"] ; Clojure language

                 ;; Application domain
                 [com.github.piotr-yuxuan/malli-cli "0.0.5"] ; Command-line processing
                 [fundingcircle/jackdaw "0.8.0"] ; Clojure API for Kafka
                 [metosin/malli "0.5.1"] ; Coerce shape and (de)code data
                 [org.clojure/data.csv "1.0.1"] ; Reader/writer of delimiter-separated data file
                 [piotr-yuxuan/closeable-map "0.35.0"] ; Bare-bone state management

                 ;; Performance and observability tools
                 [com.brunobonacci/mulog "0.8.2"] ; Versatile logging library
                 [com.brunobonacci/mulog-jvm-metrics "0.8.0"] ; JVM sampler for µ/log
                 [com.brunobonacci/mulog-filesystem-metrics "0.8.0"] ; Files system sampler for µ/log
                 [com.brunobonacci/mulog-prometheus "0.8.2"] ; Prometheus exporter for µ/log
                 [com.clojure-goes-fast/clj-async-profiler "1.0.0-alpha1"] ; Profiler
                 [com.github.jbellis/jamm "0.4.0"] ; JVM memory meter, retrieved from piotr-yuxuan/jamm
                 [com.widdindustries/tools.jvm "0.1.6"] ; JVM runtime data
                 [criterium "0.4.6"] ; Benchmarking library
                 [jmh-clojure "0.4.1"] ; JMH benchmarking for Clojure
                 [clj-commons/iapetos "0.1.12"] ; Metrics exporter to Prometheus

                 [org.clojure/math.combinatorics "0.1.6"]
                 ;; Ancillary tools
                 [camel-snake-kebab "0.4.3"] ; Case and type manipulation

                 ;; Development tools
                 [ns-tracker "0.4.0"]]
  :main piotr-yuxuan.tsv-processing.main
  :profiles {:github {:github/topics []
                      :github/private? false}
             :provided {:dependencies []}
             :dev {:global-vars {*warn-on-reflection* true}
                   :dependencies []
                   :source-paths ["src" "dev"]
                   :repl-options {:init-ns user}}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :kaocha [:test {:dependencies [[lambdaisland/kaocha "1.66.1034"]]}]}
  :repositories [["jamm" {:url "https://maven.pkg.github.com/piotr-yuxuan/jamm"
                          :username :env/GITHUB_ACTOR
                          :password :env/WALTER_GITHUB_PASSWORD}]]
  :deploy-repositories [["clojars" {:sign-releases false
                                    :url "https://clojars.org/repo"
                                    :username :env/WALTER_CLOJARS_USERNAME
                                    :password :env/WALTER_CLOJARS_PASSWORD}]
                        ["github" {:sign-releases false
                                   :url "https://maven.pkg.github.com/piotr-yuxuan/tsv-processing"
                                   :username :env/GITHUB_ACTOR
                                   :password :env/WALTER_GITHUB_PASSWORD}]])
