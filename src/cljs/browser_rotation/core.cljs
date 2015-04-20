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

(defonce users (atom []))

(defn add-user [text]
  (let [updated (conj @users text)]
    (reset! users updated)))

(defn atom-input []
   [:input {:type "text" 
            :placeholder "Developer name"
            :on-key-down #(case (.-which %)
                           13 (do
                                (add-user (-> % .-target .-value))
                                (set! (-> % .-target .-value) "")
                                )
                           nil) }])

(defn browser-rotation []
  (let [days ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday"]]
    [:span
     [:div.space
      [atom-input]]
     [:div.space
      [:table
       [:tr 
        [:th]
        (for [day days]
          ^{:key day} [:th day])]
        (map-indexed 
          (fn [i user]
            [browser-cycle user i]) @users)]]]))
      
(defn footer []
  [:ul
   [:li "Build with " [:a {:href "https://reagent-project.github.io/"} "reagent"]]
   [:li " by " [:a { :href "https://www.about.me/kitofr" } "kitofr"] " in 2015"]])

(defn home-page []
  [:div
   [:p.small-heading "Create your own browser"]
   [:p.large-heading "Rotation Schedule"]
   [:div [browser-rotation]]
   [:div.footer [footer]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

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
