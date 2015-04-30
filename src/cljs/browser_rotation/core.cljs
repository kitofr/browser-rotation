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

(def browsers (atom [{:chrome "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSx_ewOJFxPKntWJvSsEqSs1bOrbgV6OEzdG-D2lxIU1tJXIjGcRAjbvfdA" :selected? true}
                     {:firefox "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRl842DwO1aFB0s2RjP2NOIatW34mTWmXpZeL8bfcvHR82cAz-yGGSuxh1c" :selected? true}
                     {:safari "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcShE2MlWD_XfWH8DnMlckF_e9Y4OougOzzDiL_9OMP_pGjtMfYAp4TJlOzg" :selected? true}
                     {:ie "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSX1WBUmFYhJDHSA6qxeoFCN8h2zl3SLOcrXOdBmVc5Jaz3GcsAq4qeArHi" :selected? true}
                     {:opera "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSc3W-bMSPcREOssHYIso-Wh8KGQylj4FAOUG_zGNIjrik04NdvRSUxlF4G" :selected? true}
                     {:mosaik "http://crossjam.net/mpr/media/Mosaic%20Browser%20Logo.png" :selected? true}
                     {:vivaldi "https://vivaldi.com/assets/vivaldi.svg" :selected? true}
                     {:netscape "http://www.ranklogos.com/wp-content/uploads/2012/04/Netscape-logo1.jpg" :selected? true}
                     {:lynx "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTy9oS7vRhRVziK-mk-cYK4MU8yhJ4w0tnJ8US471Xb9JNy7V8k_A" :selected? true}
                     {:tor "https://www.torproject.org/images/tor-logo.jpg" :selected? true}
                     ]))

(defonce users (atom []))

(defn browser-links [n]
  (->> @browsers 
    (filter #(:selected? %))
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
      [:img {:id i :src (value link) :width 100 :height 100 :alt (k link) }]])])

(defn add-user [text]
  (let [updated (conj @users text)]
    (reset! users updated)))

(defn name-input []
   [:input {:type "text" 
            :placeholder "Developer name"
            :on-key-down #(case (.-which %)
                           13 (do
                                (add-user (-> % .-target .-value))
                                (set! (-> % .-target .-value) "")
                                )
                           nil) }])

(defn select! [id] 
  (do
    (swap! browsers update-in [id :selected?] not)))

(defn browser-selection []
  [:div 
   (map-indexed (fn [i browser]
                  (let [n (str (k browser))]
                    [:span
                     [:input {:type "checkbox" 
                              :checked (:selected? browser) 
                              :id n
                              :on-change #(select! i) }]
                     [:label {:for n}
                      [:img { :src (value browser) :width 30 :height 30 } ]]])) @browsers)])

(defn browser-rotation []
  (let [days ["Monday" "Tuesday" "Wednesday" "Thursday" "Friday"]]
    [:span
     [:div.space
      [(with-meta name-input
                  {:component-did-mount #(.focus (reagent/dom-node %))})]]
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
   [:div [browser-selection]]
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
