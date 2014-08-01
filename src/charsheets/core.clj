(ns charsheets.core)
(require '[clj-yaml.core :as yaml])

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

(defn distribute-points
  "Assign given points to random keys in the given map."
  [m points]

  (def mref
    (ref m))

  (doseq [i (range points)]
    (dosync
      (alter mref update-in
        [(rand-nth (keys (get-map-with-values-in-limit @mref)))]
        inc)
      )
    )
  @mref
  )

(defn alter-sheet
  [points sheet class sub-section]
  (dosync
    (doseq [point-key (keys points)]
      (alter sheet
        assoc-in
        [class (key sub-section) point-key]
        (get points point-key))
      )
    )
  )

(defn add-section-points-to-sheet
  "Adds section points to sheet."
  [sub-sections section sheet]
    (doseq [sub-section sub-sections]
      (let [sheet-sub-section (ref (get (get @sheet section) (key sub-section)))
            points (distribute-points @sheet-sub-section (val sub-section))]
        (alter-sheet points sheet section sub-section)
        )
      ))

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
      (println dichotomies)
      )))

(defn generate
  "Creates a new sheet."
  [dichotomies]

  (validate-input dichotomies)

  (let [type (keyword
               (clojure.string/join ""
                  (for [dichotomy (keys dichotomies)] (name dichotomy))))
        empty-sheet (get
                      (yaml/parse-string
                        (slurp
                          (clojure.java.io/resource "sheet-information.yaml")))
                      :sheet)
        grouping-effects (get
                           (yaml/parse-string
                             (slurp
                               (clojure.java.io/resource "type-effects.yaml")))
                           type)
        sheet (ref (deep-merge empty-sheet (get grouping-effects :sheet)))
        attributes-sections (get grouping-effects :attributes-ordinal)
        abilities-sections (get grouping-effects :abilities-ordinal)]

    (add-section-points-to-sheet attributes-sections :attributes sheet)
    (add-section-points-to-sheet abilities-sections :abilities sheet)

    @sheet

    ))

