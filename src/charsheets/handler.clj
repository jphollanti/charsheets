(ns charsheets.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.data.json :as json]
            [charsheets.core]))

(use 'clojure.walk)

(defroutes app-routes
  (GET "/" {params :params} (str "<h1>Hello World</h1>"
                              (pr-str
                                (charsheets.core/generate
                                  (keywordize-keys
                                    (json/read-str
                                      (get params :dichotomies)))))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
