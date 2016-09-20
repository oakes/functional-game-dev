(ns functional-game-dev.time-travel
  (:require [play-cljs.core :as p]))

(defonce history (atom []))

(defn rewind [game steps]
  (when (contains? (p/get-pressed-keys game) 32)
    (when-let [h @history]
      (when-let [s (get h (- (count h) steps 1))]
        (swap! history subvec 0 (- (count h) steps 1))
        s))))

(defn start-recording [state]
  (add-watch state :timeline
    (fn [_ _ _ new-state]
      (when-not (= new-state (last @history))
        (swap! history conj new-state)))))

