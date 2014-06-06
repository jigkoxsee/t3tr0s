(ns client.mc
	(:require-macros [hiccups.core :as hiccups])
  (:require
    [cljs.reader :refer [read-string]]
    [client.socket :refer [socket]]
		hiccups.runtime))

(def $ js/$)

;;------------------------------------------------------------
;; Stop Game page
;;------------------------------------------------------------

(hiccups/defhtml stop-html []
  [:div#inner-container
    [:div.login-5983e
      [:form
        [:button#submit.red-btn-2c9ab "STOP"]]]])

(declare init-start-page!)

(defn init-stop-page!
  "Initialize the start game page."
  []
  (.html ($ "#main-container") (stop-html))

  (.click ($ "#submit")
          #(do (.emit @socket "stop-game")
               (init-start-page!))))

;;------------------------------------------------------------
;; Start Game page
;;------------------------------------------------------------

(hiccups/defhtml start-html []
  [:div#inner-container
    [:div.login-5983e
      [:form
        [:button#submit.green-btn-f67eb "START"]]]])

(defn init-start-page!
  "Initialize the start game page."
  []
  (.html ($ "#main-container") (start-html))

  (.click ($ "#submit")
          #(do (.emit @socket "start-time")
               (init-stop-page!))))

;;------------------------------------------------------------
;; Password page
;;------------------------------------------------------------

(hiccups/defhtml password-html []
  [:div#inner-container
    [:div.login-5983e
      [:form
        [:div.input-4a3e3
          [:label.label-66a3b "MC password:"]
          [:input#password.input-48f1f {:type "password"}]]
        [:button#submit.red-btn-2c9ab "OK"]]]])

(defn on-grant-mc
  "Callback for handling the MC access grant."
  [str-data]
  (if-let [game-running (read-string str-data)]
     (init-stop-page!)
     (init-start-page!)))

(defn init-password-page!
  "Initialize the password page."
  []
  (.html ($ "#main-container") (password-html))

  ; Request access as MC when user submits password.
  (.click ($ "#submit")
          #(.emit @socket "request-mc"
                  (.val ($ "#password"))))

  ; Allow pressing enter to submit.
  (.keyup ($ "#password")
          #(if (= (.-keyCode %) 13)
             (.click ($ "submit"))))

  ; Render either the stop page or the start page
  ; when access as MC is granted.
  (.on @socket "grant-mc" on-grant-mc))

;;------------------------------------------------------------
;; Main page intializer.
;;------------------------------------------------------------

(defn init
  []
  (client.core/set-bw-background!)

  (init-password-page!)
  )

(defn cleanup
  []

  ; Leave the MC role.
  (.emit @socket "leave-mc")

  ; Destroy socket listeners.
  (.removeListener @socket "grant-mc" on-grant-mc)

  )
