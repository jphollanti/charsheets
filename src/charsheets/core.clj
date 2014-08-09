(ns charsheets.core)
(require
  '[clj-yaml.core :as yaml])


(defn weighted-rand-choice [m]
  (let [w (reductions #(+ % %2) (vals m))
        r (rand-int (last w))]
    (nth (keys m) (count (take-while #( <= % r ) w)))))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn get-map-with-values-in-limit
  [m]
  (select-keys m (for [[k v] m :when (< v 5)] k)))

(defn get-points
  [accumulation-probability remaining-points current-points]
  (let [val (- accumulation-probability (rand-int 5))
        newval (if (> (+ current-points val) 5) (- 5 current-points) val)]
    (if (< newval 1)
      1
      (if (> newval remaining-points)
        remaining-points
        newval))))

(defn distribute-points
  "Assign given points to random keys in the given map."
  [m points accumulation-probability]

  (def mref
    (ref m))

  (let [i (atom points)]
    (while (> @i 0)
      (let [rkey (rand-nth (keys (get-map-with-values-in-limit @mref)))
            assign (get-points accumulation-probability @i (get-in @mref [rkey]))]
        (dosync
          (alter mref update-in [rkey] (fn [curr] (+ curr assign))
            ))
      (swap! i (fn [curr] (- curr assign))))))
  @mref
  )

(defn flatten-keys
  ([m] (flatten-keys {} [] m))
  ([a ks m]
    (if (map? m)
      (reduce into (map (fn [[k v]] (flatten-keys a (conj ks k) v)) (seq m)))
      (assoc a ks m))))

(defn validate-input
  [dichotomies]
  (future
    (let [amount (count (keys dichotomies))
          first (name (nth (keys dichotomies) 0))
          second (name (nth (keys dichotomies) 1))
          third (name (nth (keys dichotomies) 2))
          fourth (name (nth (keys dichotomies) 3))]

      (assert (= amount 4))
      (assert (or (= first "e") (= first "i")))
      (assert (or (= second "n") (= second "s")))
      (assert (or (= third "t") (= third "f")))
      (assert (or (= fourth "j") (= fourth "p")))
      )))

(defn generate
  "Creates a new sheet."
  [dichotomies]

  (validate-input dichotomies)

  (let [type (keyword
               (clojure.string/join ""
                 (for [dichotomy (keys dichotomies)] (name dichotomy))))
        sheet-tpl (yaml/parse-string
                      (slurp
                        (clojure.java.io/resource "sheet-tpl.yaml")))
        sheet-tpl-type-overrides ((yaml/parse-string
                        (slurp
                          (clojure.java.io/resource "sheet-tpl-type-overrides.yaml")))
                       type)
        merged (deep-merge sheet-tpl sheet-tpl-type-overrides)
        meta (merged :meta)
        sheet (ref (merged :sheet))]

    (doseq [keyval (flatten-keys (meta :points))]
      (dosync
        (alter sheet assoc-in
          (key keyval)
          (distribute-points (get-in @sheet (key keyval))
            (val keyval)
            (if (get-in meta [:point-accumulation (first (key keyval))])
              (get-in @sheet [:virtues :courage])
              0)))))

    @sheet))

