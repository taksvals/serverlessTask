(ns calculate.core
  (:require [clojure.data.json :as json]
            [clojure.java.io   :as io]
            [clojure.string :as str])
  (:gen-class
   :name       calculate.core.ApiHandler
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))

(defn parse-int [s]
   (Double. (re-find  #"\d+" s)))

(defmulti calculator :operation)

(defmethod calculator :addition [expression] 
  (+ (:first expression) (:last expression)))

(defmethod calculator :subtraction [expression] 
  (- (:first expression) (:last expression)))

(defmethod calculator :multiplication [expression] 
  (* (:first expression) (:last expression)))

(defmethod calculator :division [expression] 
  (/ (:first expression) (:last expression)))

(defmethod calculator :default [_] "No such operation")

(defn descr [expression]
  (let [a  (parse-int (first expression))
        b  (parse-int ((resolve (symbol (nth expression 1)))
                       (nth expression 2)))
        c  (parse-int ((resolve (symbol (nth expression 3)))
                       (nth expression 4)))
        l (parse-int (last expression))]
    (- 
     (* b b) 
     (* 4 a (- c l)))))

(defn root [descr expression sign]
  (let [a (parse-int (first expression))
        b (parse-int ((resolve (symbol (nth expression 1)))
                       (nth expression 2)))]
    (/ 
     ((resolve (symbol sign)) 
      (- b) 
      (Math/sqrt descr)) 
     (* 2 a))))

(defn quadratic [expression]
  (let [d (descr expression)]
           (if (< d 0)
             "Expression has no solutions"
             (if (= d 0.)
               (root d expression "+")
               [(root d expression "+")
                (root d expression "-")]))))

(defn operation [operator]
  (condp = operator
    "+" :addition
    "-" :subtraction
    "/" :division
    "*" :multiplication))

(defn expression-heandler[expr]
  (let [values (str/split expr #" ")]
    (if (> (count values) 3)
      (quadratic values)
      (try
        (calculator {:operation (operation (second values))
                     :first     (Double.   (first values))
                     :last      (Double.   (last values))})
        (catch Exception e (str "Error: " (.getMessage e)))))))


(comment
  (calculator {:operation :addition :first 5 :last 7})
  (calculator {:operation :subtraction :first 5 :last 7})
  (calculator {:operation :multiplication :first 5 :last 7})
  (calculator {:operation :division :first 2 :last 0})
  (quadratic ["4x2" "+" "4x" "+" "1" "0"])
  (= 0. (descr ["4x2" "+" "4x" "+" "2" "1"]))
  (root (descr ["4x2" "+" "4x" "+" "2" "1"])
       ["4x2" "+" "4x" "+" "2" "1"] 
        "+")
  (expression-heandler "5 / 0"))

(defn handle-request [handler]
  (fn [_ input-stream output-stream context]
    (with-open [in  (io/reader input-stream)
                out (io/writer output-stream)]
      (let [request (json/read in :key-fn keyword)]
        (-> request
            (handler context)
            (json/write out))))))

(def -handleRequest
  "Lambda"
  (handle-request
   (fn [event context]
     (prn event)
     {:status 200
      :body   (expression-heandler
               (:expr 
                (json/read-str 
                 (:body event) 
                 :key-fn keyword)))})))
