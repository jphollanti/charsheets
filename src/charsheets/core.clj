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

(defn distribute-points
  "Assign given points to random keys in the given map."
  [m points]

  (def mref
    (ref m))

  (doseq [i (range points)]
    (dosync
      (alter mref update-in
        [(rand-nth (keys m))]
        inc)
      )
    )
  @mref
  )

(defn alter-sheet
  [points sheet class section]
  (dosync
    (doseq [point-key (keys points)]
      (alter sheet
        assoc-in
        [class (key section) point-key]
        (get points point-key))
      )
    )
  )

(defn add-section-points-to-sheet
  "Adds section points to sheet."
  [sections class sheet]
    (doseq [section sections]
      (let [sheet-section (ref (get (get @sheet class) (key section)))
            points (distribute-points @sheet-section (val section))]
        (alter-sheet points sheet class section)
        )
      ))

(defn generate
  "Creates a new sheet."
  [dichotomies]
  (println dichotomies)

  (comment
    (assert
      (>
        (count dichotomies) 4)))

  (let [empty-sheet (get
                      (yaml/parse-string
                        (slurp "./resources/sheet-information.yaml"))
                      :sheet)
        grouping-effects (get
                           (yaml/parse-string
                             (slurp "./resources/grouping-effects.yaml"))
                           :entj)
        sheet (ref (deep-merge empty-sheet (get grouping-effects :sheet)))
        attributes-sections (get grouping-effects :attributes-ordinal)
        abilities-sections (get grouping-effects :abilities-ordinal)]

    (add-section-points-to-sheet attributes-sections :attributes sheet)
    (add-section-points-to-sheet abilities-sections :abilities sheet)

    @sheet

    ))

