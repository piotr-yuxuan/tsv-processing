(ns piotr-yuxuan.tsv-processing.wip
  (:require [clojure.data :as data]
            [clojure.math.combinatorics :as c]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest testing is]]
            [malli.generator :as mg]))

(defn -main
  [])

(def direction
  "^v><")

(def Direction
  (let [move [:enum \v \^ \> \<]]
    [:sequential move]))

(mg/generate Direction)

(def position-updates
  ;; [x, y], where x the horizontal axis (pointing to the right) and y the vertical axis (pointing to above).
  {\v [0 -1]
   \^ [0 1]
   \> [1 0]
   \< [-1 0]})

(def default-position
  [0 0])

(defn direction->path
  [init-position direction]
  (reductions #(mapv + %1 (get position-updates %2))
              init-position
              direction))

(defn number-of-houses-visited
  ([direction]
   (number-of-houses-visited 1 default-position direction))
  ([init-position direction]
   (number-of-houses-visited 1 init-position direction))
  ([number-of-riders init-position direction]
   (->> (map-indexed vector direction)
        (reduce (fn [acc [index move]]
                  (update acc (mod index number-of-riders) conj move))
                (zipmap (range number-of-riders)
                        (repeat [])))
        (mapcat (fn [[_rider-id rider-own-direction]]
                  (direction->path init-position rider-own-direction)))
        distinct
        count)))

(comment
  ;; Response 1 & 2:
  (number-of-houses-visited default-position direction)
  (number-of-houses-visited 1 [0 0] "") ; Starting position is included
  (number-of-houses-visited 1 [0 0] "^^vv<><>")
  (number-of-houses-visited 2 [0 0] "^<v>"))

(defn number-of-houses-visited-2
  [rider-ids->init-position direction]
  (->> (map-indexed vector direction)
       (reduce (fn [acc [index move]]
                 (update acc (mod index (count rider-ids->init-position)) conj move))
               (zipmap (range (count rider-ids->init-position))
                       (repeat [])))
       (mapcat (fn [[rider-id rider-own-direction]]
                 (direction->path (get rider-ids->init-position rider-id)
                                  rider-own-direction)))
       distinct
       count))

(comment
  (number-of-houses-visited 2 [0 0] "^^vv<><>")
  (= (number-of-houses-visited-2
       {0 [0 100]
        1 [0 0]}
       "^^vv<><>")
     (* 2 (number-of-houses-visited 1 [0 0] "^^vv<><>"))))

;; We first only want an answer, whatever the cost.

(defn set-distance
  "A bit like [Levenshtein distance](https://en.wikipedia.org/wiki/Levenshtein_distance) but for sets. Mathematicians would probably call it a metric."
  [left right]
  (->> (data/diff left right)
       butlast
       (map count)
       (reduce +)))

(defn invalid-averages
  [threshold queries averages]
  (some (fn [q]
          (when-not (some (fn [a]
                            (<= (set-distance q a) threshold))
                          averages)
            q))
        queries))

(comment
  (invalid-averages 1 [#{1 2} #{1 2 4 6}]
                    ;; averages:
                    [#{1 2 4}])

  (invalid-averages 2 [#{1 2} #{1 2 4 6}]
                    ;; averages:
                    [#{1 4}]))

(def q1 #{1 2 4})
(def q2 #{1 2 4 6})
(def q3 #{1 8})
(def q4 #{1 2 4 8})
(def q5 #{1 2 3 5 7 8})
(def q6 #{2 4 6})
(def q7 #{2 3 5 6 7})
(def q8 #{3 6})

(def queries
  #{q1 q2 q3 q4 q5 q6 q7 q8})

(defn query-distances
  "Warning kids: never ever do this in a coding interview ^_^"
  [threshold queries]
  (reduce (fn [acc [left right]]
            (let [close-enough? (<= (set-distance left right) threshold)]
              ;; Shall we use Bloom filters or anything similar to scale this?
              (cond-> acc
                close-enough? (update left conj right)
                close-enough? (update right conj left))))
          (->> queries
               (map (juxt identity hash-set))
               (into {}))
          (mapcat #(c/combinations queries %)
                  (range 2 (inc threshold)))))

(comment
  (query-distances 2 queries))

(defn nearby-existing-queries
  "Return a set of existing queries whose distance to any other query is lower than the threshold.

  One minimum may bot be necessarily unique. Warning kids: never ever do this in a coding interview ^_^"
  ([threshold queries]
   (nearby-existing-queries threshold queries []))
  ([threshold queries sorted-chain]
   (if (empty? queries)
     (set sorted-chain)
     (let [longest-query (reduce (fn longest [q i]
                                   ;; Return one query maximising the `order`. Not necessarily a total ordering, so not necessarily unique.
                                   (if (< (count q)
                                          (count i))
                                     i
                                     q))
                                 queries)]
       (recur threshold
              ;; We took the longest, so nearby queries are necessarily within reach of each other.
              (apply disj (disj queries longest-query)
                     (get (query-distances threshold queries)
                          longest-query))
              (conj sorted-chain longest-query))))))

(comment
  (= queries (nearby-existing-queries 1 queries))
  (= #{q5} (nearby-existing-queries 7 queries))
  (nearby-existing-queries 2 queries))

(defn average-synthetic-query
  "Given a set of queries, return some synthetic query with minimal distance to all others under `threshold`."
  [threshold queries]
  (loop [queries queries
         ret #{}
         infinite-loop-breaker 50]
    (let [by-frequency (->> (frequencies (apply concat queries))
                            (reduce (fn [acc [a n]]
                                      (update acc n (fnil conj #{}) a))
                                    {})
                            (remove (comp ret key))
                            (into {}))
          highest-frequence (apply max (keys by-frequency))
          tentative-query (set/union ret (get by-frequency highest-frequence))]
      (cond (every? #(<= (set-distance tentative-query %) threshold) queries)
            tentative-query

            (pos? infinite-loop-breaker)
            (recur queries
                   tentative-query
                   (dec infinite-loop-breaker))

            :else
            (do (.println System/err "Breaking infinite loop, you could be fired for that.")
                (first (sort-by count queries)))))))

(comment
  (= (average-synthetic-query 2 [#{1 2 3}
                                 #{2 3 4}
                                 #{3 4 1}
                                 #{4 1 2}])
     #{1 2 3 4})

  (= (average-synthetic-query 2 [#{2 3 1}
                                 #{2 3 4}
                                 #{2 3 5}])
     #{2 3})

  (average-synthetic-query 2 #{#{1 4 2 8} #{1 8} #{1 4 6 2} #{1 4 2}}))

(defn nearby-synthetic-queries
  "Return a set of existing queries whose distance to any other query is lower than the threshold.

  One minimum may bot be necessarily unique. Warning kids: never ever do this in a coding interview ^_^"
  ([threshold queries]
   (nearby-synthetic-queries threshold queries []))
  ([threshold queries sorted-chain]
   (if (empty? queries)
     (set sorted-chain)
     (let [longest-query (reduce (fn longest [q i]
                                   ;; Return one query maximising the `order`. Not necessarily a total ordering, so not necessarily unique.
                                   (if (< (count q)
                                          (count i))
                                     i
                                     q))
                                 queries)
           nearby-distances (get (query-distances threshold queries) longest-query)]
       (recur threshold
              ;; We took the longest, so nearby queries are necessarily within reach of each other.
              (apply disj queries nearby-distances)
              (conj sorted-chain (average-synthetic-query threshold nearby-distances)))))))

(comment
  (= queries (nearby-synthetic-queries 1 queries))
  (= #{#{2}} (nearby-synthetic-queries 7 queries))
  (nearby-synthetic-queries 2 queries)

  (double
    (/ (reduce + (map count queries))
       (count queries))))

(defn query
  [market-size]
  (->> (range market-size)
       shuffle
       ;; At least about one asset
       (take (inc (rand-int market-size)))
       set))

(def big-queries
  (repeatedly 100 #(query 100)))

(comment
  (count (nearby-synthetic-queries 10 big-queries)))
