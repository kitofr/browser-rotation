(ns browser-rotation.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react])
    (:import goog.History))

;; -------------------------
;; Views

(defn k [pair]
  (-> pair keys first))

(defn value [pair]
  (-> pair vals first))

(defn browser-links [n]
  (->> [{:chrome "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSx_ewOJFxPKntWJvSsEqSs1bOrbgV6OEzdG-D2lxIU1tJXIjGcRAjbvfdA" }
        {:firefox "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRl842DwO1aFB0s2RjP2NOIatW34mTWmXpZeL8bfcvHR82cAz-yGGSuxh1c" }
        {:safari "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcShE2MlWD_XfWH8DnMlckF_e9Y4OougOzzDiL_9OMP_pGjtMfYAp4TJlOzg" }
        {:ie "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSX1WBUmFYhJDHSA6qxeoFCN8h2zl3SLOcrXOdBmVc5Jaz3GcsAq4qeArHi" }
        {:opera "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSc3W-bMSPcREOssHYIso-Wh8KGQylj4FAOUG_zGNIjrik04NdvRSUxlF4G" }]
    cycle
    (drop n)
    (take 5)
    vec))

(defn browser-cycle [user i]
  ^{:key user} 
  [:tr 
   [:td user]
   (for [link (browser-links i)]
     [:td 
      ^{:key (k link)}
      [:img {:id i :src (value link) :width 100 :height 100 }]])])

(defn add-user [users text]
  (swap! users conj text))

(defn atom-input [users]
   [:input {:type "text" 
            :placeholder "Developer name"
            :on-key-down #(case (.-which %)
                           13 (add-user users (-> % .-target .-value))) }])

(defn browser-rotation [users]
  (let [days ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday"]]
       [users (atom ["Kristoffer" 
                     "Lennart"
                     "Dennis"
                     "Victoria"
                     "Martin"
                     "Mikael"
                     "Kalle"])]
    [:div
    [atom-input @users]
    [:table
     [:tr 
      [:th]
      (for [day days]
        ^{:key day} [:th day])
        (map-indexed 
          (fn [i user]
            [browser-cycle user i]) users)]]]))
      
(defn home-page []
  [:div [:h2 "Welcome to browser-rotation"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:h3 "Browser schedule"]
   [:div [browser-rotation]]])

(defn about-page []
  [:div [:h2 "About browser-rotation"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
