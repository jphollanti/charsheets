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

(defn add-values
  "Adds values to sheet."
  [ordinals class sheet empty-sheet]
    (doseq [ordinal ordinals]
      (dorun
        (for
          [i (range (val ordinal))]

          (let [rand-ordinal (rand-nth (keys (get (get empty-sheet class) (key ordinal))))]
            (dosync
              (alter sheet update-in
                [class (key ordinal) rand-ordinal]
                inc)
              )
            )

          )
        ))
    )

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

