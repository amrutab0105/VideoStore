(ns videostore.database
(:gen-class)
(:import (javax.swing JButton JFrame))
(:require [seesaw.core :as seesaw]
           [seesaw.icon :as seesaw.icon]
           [seesaw.swingx :as swingx]
           [seesaw.table :as table]
           [seesaw.mig :as mig]))

;-------------------------------------------------------------------------------------------------------------------------------------

;Count number of lines present in the database
(defn no-of-lines [filename] (with-open [rdr (clojure.java.io/reader filename)] (count (line-seq rdr))))

;read nth line from the database
(defn read-nth-line [file line-number] (with-open [rdr (clojure.java.io/reader file)] (nth (line-seq rdr) (dec line-number))))

;create a vector containing all the rows from the database
(defn get-row-map [filename counter rmap]
 (if(<= counter (no-of-lines filename))  (recur filename (inc counter) (conj rmap (read-string (read-nth-line filename counter)))) rmap))

(defn getrows [filename]   (get-row-map filename 1 []))

;create movies-table from all the values present in the movies database
(def movies-table (seesaw/table :model [:columns [:ID :mname :price :quantity] :rows (getrows "moviesdata.txt")]
                                :selection-mode :single))

;create renter-table from all the values present in the renter database
(def renter-table  (seesaw/table :model [:columns [:mname :rname :renting-date :due-date] :rows (getrows "renterdata.txt")]
                                 :selection-mode :single))

;write to movie database
(defn wmovies-database [info] (with-open [w (clojure.java.io/writer "moviesdata.txt" :append true)] (binding [*out* w] (prn info))))

;write to renter database
(defn wrenter-database [info] (with-open [w (clojure.java.io/writer "renterdata.txt" :append true)] (binding [*out* w] (prn info))) )


;inserting updated movies-table in the database
(defn insert-updated-movies-data [row-count ini]
  (let [value (table/value-at movies-table ini)]
    (wmovies-database value)
       (cond
        (< ini row-count) (recur row-count (inc ini))
        (= ini row-count) (wmovies-database (table/value-at movies-table (inc ini))))))

;inserting updated renter-table in the database
(defn insert-updated-renter-data [row-count ini]
(let [value (table/value-at renter-table ini)]
(wrenter-database value)
(cond
(< ini row-count) (recur row-count (inc ini))
(= ini row-count) (wrenter-database (table/value-at renter-table (inc ini))))))



;update quantity in movies database after returning a rented movie
(defn update-movie [selected-movie values]
(let [ori-quantity (:quantity (table/value-at movies-table selected-movie)) row-count (dec (table/row-count movies-table))]
  (wrenter-database values)
  (table/update-at! movies-table selected-movie {:quantity (str (dec (read-string ori-quantity)))})
  (clojure.java.io/delete-file "moviesdata.txt")
  (insert-updated-movies-data (dec row-count) 0)))


;add new movie to databases
(defn add [selected-movie table values event]
(let [row-count (table/row-count table)]
(table/insert-at! table row-count values)
(cond
(= table movies-table) (wmovies-database values)
(= table renter-table) (update-movie selected-movie values))(seesaw/dispose! (seesaw/to-root event)) ))


;find a movie by name in the database and increment its quantity
(defn find-movie [mname counter]
(cond
(= mname (:mname (table/value-at movies-table  counter)))
(table/update-at! movies-table counter {:quantity (str (inc  (read-string (:quantity (table/value-at movies-table counter)))))})
(not= mname (:mname (table/value-at movies-table  counter))) (recur mname (inc counter))))


;return a movie
(defn return-movie [selected-row]
(let [ mname (:mname (table/value-at renter-table selected-row )) row-count-renter (dec (table/row-count renter-table)) row-count-movies  (dec (table/row-count movies-table))]
(table/remove-at! renter-table selected-row)
(find-movie mname 0)
(clojure.java.io/delete-file "renterdata.txt")
(cond
(= 0 (table/row-count renter-table)) (spit "renterdata.txt" "")
(not= 0 (table/row-count renter-table)) (insert-updated-renter-data (- row-count-renter 2) 0))
(clojure.java.io/delete-file "moviesdata.txt")
(insert-updated-movies-data (dec row-count-movies) 0)))

;update the quantity of the selected movie (increment or decrement by 1)
(defn update-quantity [selected-row quantity]
(let [row-count  (dec (table/row-count movies-table))]
(table/update-at! movies-table selected-row quantity )
(clojure.java.io/delete-file "moviesdata.txt")
(insert-updated-movies-data (dec row-count) 0)))

;update the price of the selected movie.
(defn update-price [selected-row event]
(let [row-count  (dec (table/row-count movies-table))]
(table/update-at! movies-table selected-row {:price (seesaw/input "Enter Price")} )
(clojure.java.io/delete-file "moviesdata.txt")
(insert-updated-movies-data (dec row-count) 0)))

;get movie names in a vector
(defn list-names [rowcount vmap]
(loop [ini 0] (if (> ini rowcount) (reverse vmap)
              (list-names (dec rowcount) (conj vmap (:mname (table/value-at movies-table rowcount)))))))

;get movie IDs in a vector
(defn list-ID [rowcount IDmap]
(loop [ini 0] (if (> ini rowcount) (reverse IDmap)
              (list-ID (dec rowcount) (conj IDmap (:ID (table/value-at movies-table rowcount)))))))
