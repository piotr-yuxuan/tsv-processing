(ns piotr-yuxuan.memory-meter
  "This basically provides the same functionality as clj-memory-meter,
  but ships a more up-to-date version."
  (:import (org.github.jamm MemoryMeter MemoryMeter$Builder)))

(defonce singleton
  (delay (.build ^MemoryMeter$Builder (MemoryMeter/builder))))

(defn measure
  "Return the memory usage of object including referenced objects."
  ^long [^Object o]
  (if o
    (.measureDeep ^MemoryMeter @singleton o)
    0))

(defn measure-shallow
  "Return the shallow memory usage of object. Doesn't include referenced objects."
  ^long [^Object o]
  (if o
    (.measure ^MemoryMeter @singleton o)
    0))

(measure (byte 1))
