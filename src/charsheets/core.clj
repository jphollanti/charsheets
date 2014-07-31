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

(defn update-vals [map vals f]
  (reduce #(update-in % [%2] f) map vals))

(defn add-values
  "Adds values to sheet."
  [ordinals class sheet empty-sheet]
    (doseq [ordinal ordinals]
      (let [sheet-section (ref (get (get @sheet class) (key ordinal)))
            rand (distribute-points @sheet-section (val ordinal))]
        (dosync
          (doseq [mykey (keys rand)]
            (alter sheet
              assoc-in
              [class (key ordinal) mykey]
              (get rand mykey))
            )

          )
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
        sheet-tpl (deep-merge empty-sheet (get grouping-effects :sheet))
        attributes-ordinal (get grouping-effects :attributes-ordinal)
        abilities-ordinal (get grouping-effects :abilities-ordinal)]

    (def sheet
      (ref
        sheet-tpl))

    (add-values attributes-ordinal :attributes sheet empty-sheet)
    (add-values abilities-ordinal :abilities sheet empty-sheet)

    @sheet

    ))

