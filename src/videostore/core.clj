(ns videostore.core
(:gen-class)
(:import (javax.swing JButton JFrame))
(:require [videostore.database :as db]
           [seesaw.core :as seesaw]
           [seesaw.icon :as seesaw.icon]
           [seesaw.swingx :as swingx]
           [seesaw.table :as table]
           [seesaw.mig :as mig]))

;----------------------------------------------------------------------------------------------------------------------------------------

;Basic display frame
(defn display  [content width height]
(let [window (seesaw/frame :title "Video Store" :content content :width width :height height)] (seesaw/show! window)))

;Add name, price and quanntity of a new movie that is to be added in the inventory and assign an ID to it.
(defn new-movie-details [selected-movie event]
(let [mname (seesaw/text :text "Enter movie" :columns 30)
      price (seesaw/text :text "00.00" :columns 5)
      quantity (seesaw/text :text "0" :columns 5)]
(if (not= 0 selected-movie) (seesaw/dispose! (seesaw/to-root event)))
    (mig/mig-panel :background :white :border "Movie Details" :constraints ["wrap 2" "[30]40[160]"] :items
    [["Movie Name"] [mname]
     ["Price ($)" ] [price]
     ["Quantity"  ] [quantity]
     [(seesaw/button :text "Cancel" :listen [:action (fn [e] (seesaw/dispose! (seesaw/to-root e)))])]
     [(seesaw/button :text "Add" :listen [:action (fn [event] (db/add selected-movie db/movies-table
                                                             {:ID (str (inc (table/row-count db/movies-table)))
                                                              :mname (seesaw/value mname)
                                                              :price (seesaw/value price)
                                                              :quantity(seesaw/value quantity)} event))])]])))

;Calculate the due-date to return  movie by adding 14 days to renting-date
(defn get-dueDate [rdate]
(let [rdate-vector (clojure.string/split rdate #"/")
      due-date  (+ 14 (read-string (second rdate-vector)))]
(if (< due-date 30)
    (clojure.string/join "/" [(first rdate-vector) (str due-date) (last rdate-vector)])
    (clojure.string/join "/" [(str (+ 1 (read-string (first rdate-vector)))) (str (- due-date 30)) (last rdate-vector)]))))

;movie-name, renter-name and renting-date required to be filled while renting a movie
(defn rent-movie-details [selected-movie event]
(seesaw/dispose! (seesaw/to-root event))
(let [mname (seesaw/label :text (:mname (table/value-at db/movies-table selected-movie)))
      rname (seesaw/text :text "FirstName LastName" :columns 30)
      renting-date (seesaw/text :text "MM/DD/YYYY" :columns 10)
      due-date (seesaw/text :text "" :columns 10)]
(mig/mig-panel :border "Renting Details"    :constraints ["wrap 2" "[30]40[160]"]   :items
[["Movie Name"   ]  [mname]
 ["Renter's Name"]  [rname]
 ["Renting Date" ]  [renting-date]
 [(seesaw/button :text "Rent" :listen [:action (fn [event] (db/add selected-movie db/renter-table
                                                                   {:mname (seesaw/value mname)
                                                                    :rname (seesaw/value rname)
                                                                    :renting-date (seesaw/value renting-date)
                                                                    :due-date (get-dueDate (seesaw/value renting-date))} event))])]
 [(seesaw/button :text "Cancel" :listen [:action (fn [e] (seesaw/dispose! (seesaw/to-root e)))])]])))



;Table Widget to display available movies and rented movies.
(defn table-widget [table]
(let[top (seesaw/label :background "#ADD8E6" :foreground :black  :text "Select a Movie:" :font "ARIAL-BOLD-20")
    left (seesaw/vertical-panel :background :white :items [(seesaw/scrollable table)])
    right (if (= table db/movies-table)
(seesaw/grid-panel :background :white :columns 1 :items[
(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Rent Movie  "
:listen [:action (fn [event] (if-let [s (seesaw/selection table)] (display (rent-movie-details (seesaw/selection table) event) 350 250)
                                                                  (seesaw/alert "Select a movie!")))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! %  :background "#ADD8E6" :foreground :black)])

(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Update Price  "
:listen [:action (fn [event] (if-let [s (seesaw/selection table)] (db/update-price (seesaw/selection table) event)
                                                                  (seesaw/alert "Select a Movie!")))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! %  :background "#ADD8E6" :foreground :black)])

(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Add Copy  "
:listen [:action (fn [event] (if-let [s (seesaw/selection table)] (db/update-quantity (seesaw/selection table)
                              {:quantity (str (inc  (read-string (:quantity (table/value-at db/movies-table (seesaw/selection table))))))})
                              (seesaw/alert "Select a movie!")))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! %  :background "#ADD8E6" :foreground :black)])

(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Remove Copy  "
:listen [:action (fn [event] (if-let [s (seesaw/selection table)]
                                (db/update-quantity (seesaw/selection table) {:quantity (str (dec (read-string (:quantity (table/value-at db/movies-table (seesaw/selection table))))))})
                                (seesaw/alert "Select a movie!")))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! %  :background "#ADD8E6" :foreground :black)])

(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Cancel  "
:listen [:action (fn [event] (seesaw/dispose! (seesaw/to-root event) ))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! %  :background "#ADD8E6" :foreground :black)])])

(seesaw/grid-panel :background :white :columns 1 :items
 [(seesaw/button  :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Return Movie"
:listen [:action (fn [event] (if-let [s (seesaw/selection table)] (db/return-movie (seesaw/selection table))
                                                                  (seesaw/alert "Select a Movie!")))

         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])

(seesaw/button :background "#ADD8E6":foreground :black  :font "ARIAL-20" :text "Cancel"
                                    :listen [:action (fn [event] (seesaw/dispose! (seesaw/to-root event)))
                         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])]))

     bottom (seesaw/left-right-split left right :divider-location 0.65)     ]
    (seesaw/top-bottom-split top bottom :divider-location 0.15)))


;Display movie-name, price and quantity.
(defn show-values [value]
(let [mname (seesaw/label :text (:mname value))
        price (seesaw/label :text (:price value)  )
        quantity (seesaw/label :text (:quantity value))
        ok (seesaw/button :text "OK" :listen [:action (fn[e](seesaw/dispose! (seesaw/to-root e)))])]
(mig/mig-panel :background :white :constraints ["wrap 2"] :items
[["Movie Name"] [mname]
 ["Price ($)"] [price]
 ["Quantity"] [quantity] [ok]])))

;Search movie by ID
(defn find-by-ID [ID e] (seesaw/dispose! (seesaw/to-root e))
(display (show-values (table/value-at db/movies-table (dec ID))) 250 150))

;Search movie by name
(defn find-by-mname [mname counter e] (seesaw/dispose! (seesaw/to-root e))
(cond
(= mname (:mname (table/value-at db/movies-table  counter))) (display (show-values (table/value-at db/movies-table counter)) 250 150)
(not= mname (:mname (table/value-at db/movies-table  counter))) (recur mname (inc counter) e)))

;Display a list of names and IDs of all the available movies to the user as a search list.
(defn find-movie-GUI []
(let [mname-label (seesaw/label :background :white :text "Select Movie:")
      list-box  (seesaw/listbox :model (vec (map :mname (db/getrows "moviesdata.txt"))))
      search-by-name (seesaw/button :background "#ADD8E6" :foreground :black :text "Search"
                      :listen [:action (fn[e] (if-let [s (seesaw/selection list-box)]
                                              (find-by-mname (seesaw/selection list-box) 0 e)
                                              (seesaw/alert "Select a Movie!")))
                               :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                               :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])
      mname (seesaw/left-right-split mname-label (seesaw/scrollable list-box) :divider-location 0.35)

      ids-label (seesaw/label :background :white :text "Select ID")
      id-list (seesaw/listbox :model (vec (map :ID (db/getrows "moviesdata.txt"))))
      search-by-id (seesaw/button :background "#ADD8E6" :foreground :black :text "Search"
                   :listen [:action (fn [e] (if-let [s (seesaw/selection id-list)]
                                            (find-by-ID (read-string (seesaw/selection id-list)) e)
                                            (seesaw/alert "Select a Movie!")))
                            :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                            :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])
      id (seesaw/left-right-split ids-label (seesaw/scrollable id-list) :divider-location 0.99)
      name-search  (seesaw/left-right-split mname search-by-name :divider-location 0.60)
      id-search (seesaw/left-right-split id search-by-id :divider-location 0.60)
      close (seesaw/button :background :black :foreground :white :text "Close"
            :listen [:action (fn [e] (seesaw/dispose! (seesaw/to-root e)))])

      idSearch-nameSearch (seesaw/top-bottom-split name-search id-search :divider-location 0.99)]

(seesaw/top-bottom-split idSearch-nameSearch close :divider-location 0.80)))

;list of options on the home-page
(def home-page-options (let
[vertical-panel
(seesaw/vertical-panel :background "#ADD8E6" :items [
(seesaw/button :background "#ADD8E6" :foreground :black  :font "ARIAL-20" :text "Available Movies  "
                :listen [:action (fn [event] (display (table-widget db/movies-table) 600 450))
                         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])
(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Rented Movies    "
                :listen [:action (fn [event] (display (table-widget db/renter-table) 600 250))
                         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])
(seesaw/button :background "#ADD8E6" :foreground :black  :font "ARIAL-20" :text "Search Movies    "
                :listen [:action (fn [event] (display (find-movie-GUI) 850 250))
                         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])
(seesaw/button :background "#ADD8E6" :foreground :black :font "ARIAL-20" :text "Exit                    "
                :listen [:action (fn [event](seesaw/dispose! (seesaw/to-root event) ))
                         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
                         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])])

 add-new-movie
(seesaw/button :background "#ADD8E6" :foreground :black  :font "ARIAL-20" :text "Add New Movie  "
:listen [:action (fn [event] (display (new-movie-details 0 event) 350 250))
         :mouse-entered #(seesaw/config! % :background "#0000A0" :foreground :white )
         :mouse-exited #(seesaw/config! % :background "#ADD8E6" :foreground :black)])

 image (seesaw/label :icon (seesaw.icon/icon "videostore/upcoming-movies.png") )

 image-button (seesaw/top-bottom-split image add-new-movie )
 ] (seesaw/left-right-split vertical-panel image-button)))


;Home-page
(def home-page
  (mig/mig-panel :background :white :constraints ["wrap 1"] :items [
  [(seesaw/label :icon (seesaw.icon/icon "videostore/title.png"))]
  [home-page-options]]))

;Display the home-page
(defn display-home [] (display home-page 860 460))

(defn -main
  [& args]
  (display-home))
