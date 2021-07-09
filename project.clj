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
                 [org.clojure/clojure "1.10.3"] ; Clojure language

                 ;; Application domain
                 [com.github.piotr-yuxuan/malli-cli "0.0.5"] ; Command-line processing
                 [fundingcircle/jackdaw "0.8.0"] ; Clojure API for Kafka
                 [metosin/malli "0.5.1"] ; Coerce shape and (de)code data
                 [org.clojure/data.csv "1.0.0"] ; Reader/writer of delimiter-separated data file
                 [piotr-yuxuan/closeable-map "0.35.0"] ; Bare-bone state management

                 ;; Ancillary tools
                 [camel-snake-kebab "0.4.2"] ; Case and type manipulation

                 ;; Performance and observability tools
                 [com.brunobonacci/mulog "0.8.0"] ; Versatile logging library
                 [com.clojure-goes-fast/clj-async-profiler "0.5.0"] ; Profiler
                 [com.github.jbellis/jamm "0.4.0"] ; JVM memory meter, retrieved from piotr-yuxuan/jamm
                 [com.widdindustries/tools.jvm "0.1.2"] ; JVM runtime data
                 [criterium "0.4.6"] ; Benchmarking library
                 [jmh-clojure "0.4.0"] ; JMH benchmarking for Clojure
                 ]
  :java-agents [[com.github.jbellis/jamm "0.4.0"]]
  :main piotr-yuxuan.tsv-processing.main
  :profiles {:github {:github/topics []}
             :provided {:dependencies []}
             :dev {:global-vars {*warn-on-reflection* true}
                   :dependencies []}
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=false"
                                  "-Dclojure.compiler.direct-linking=true"]}
             :kaocha [:test {:dependencies [[lambdaisland/kaocha "1.0-612"]]}]}
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
