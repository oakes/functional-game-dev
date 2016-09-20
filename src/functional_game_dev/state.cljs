(ns functional-game-dev.state
  (:require [play-cljs.core :as p]
            [functional-game-dev.utils :as u]))

(defn initial-state [game]
  {:current [:div {}]
   :x-velocity 0
   :y-velocity 0
   :x -100
   :y 200
   :can-jump? false
   :direction :right
   :image (p/load-image game u/image-url)
   :map (p/load-tiled-map game u/map-name)})

(defn move
  [{:keys [x y can-jump?] :as state} game]
  (let [delta-time (min (p/get-delta-time game) 100)
        x-velocity (u/get-x-velocity game state)
        y-velocity (+ (u/get-y-velocity game state) u/gravity)
        x-change (* x-velocity delta-time)
        y-change (* y-velocity delta-time)]
    (if (or (not= 0 x-change) (not= 0 y-change))
      (assoc state
             :x-velocity (u/decelerate x-velocity)
             :y-velocity (u/decelerate y-velocity)
             :x-change x-change
             :y-change y-change
             :x (+ x x-change)
             :y (+ y y-change)
             :can-jump? (if (neg? y-velocity) false can-jump?))
      state)))

(defn prevent-move
  [{:keys [x y x-change y-change] :as state} game]
  (let [old-x (- x x-change)
        old-y (- y y-change)
        up? (neg? y-change)]
    (merge state
      (when (u/touching-tile? (:map state) 1 (+ x u/koala-offset) old-y u/koala-width u/koala-height)
        {:x-velocity 0 :x-change 0 :x old-x})
      (when (u/touching-tile? (:map state) 1 (+ old-x u/koala-offset) y u/koala-width u/koala-height)
        {:y-velocity 0 :y-change 0 :y old-y :can-jump? (not up?)})
      (when (> y (- (p/get-height game) u/koala-height))
        {:y-velocity 0 :y-change 0 :y old-y :can-jump? (not up?)}))))

(defn animate
  [{:keys [x-velocity y-velocity image] :as state}]
  (let [direction (u/get-direction state)]
    (-> state
        (assoc :current
          (cond
            (not= y-velocity 0)
            (if (= direction :right)
              [:image {:value image :swidth u/source-width :sheight u/source-height :sx u/source-width
                       :width u/koala-width :height u/koala-height}]
              [:image {:value image :swidth u/source-width :sheight u/source-height :sx u/source-width :scale-x -1
                       :width (- u/koala-width) :height u/koala-height}])
            ;(not= x-velocity 0)
            ;(if (= direction :right) walk-right walk-left)
            :else
            (if (= direction :right)
              [:image {:value image :swidth u/source-width :sheight u/source-height
                       :width u/koala-width :height u/koala-height}]
              [:image {:value image :swidth u/source-width :sheight u/source-height :scale-x -1
                       :width (- u/koala-width) :height u/koala-height}])))
        (assoc :direction direction))))

