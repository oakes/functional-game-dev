(ns functional-game-dev.utils
  (:require [play-cljs.core :as p]))

(def ^:const view-size 500)
(def ^:const duration 0.15)
(def ^:const damping 0.1)
(def ^:const max-velocity 0.5)
(def ^:const max-jump-velocity (* max-velocity 7))
(def ^:const deceleration 0.9)
(def ^:const gravity 0.2)
(def ^:const image-url "koalio.png")
(def ^:const map-name "level1")
(def ^:const source-width 18)
(def ^:const source-height 26)
(def ^:const koala-width (* source-width 1))
(def ^:const koala-height (* source-height 1))
(def ^:const koala-offset (- (/ view-size 2) (/ koala-width 2)))

(defn decelerate
  [velocity]
  (let [velocity (* velocity deceleration)]
    (if (< (Math/abs velocity) damping)
      0
      velocity)))

(defn get-x-velocity
  [game {:keys [x-velocity]}]
  (cond
    (contains? (p/get-pressed-keys game) 37)
    (* -1 max-velocity)
    (contains? (p/get-pressed-keys game) 39)
    max-velocity
    :else
    x-velocity))

(defn get-y-velocity
  [game {:keys [y-velocity can-jump?]}]
  (cond
    (and can-jump? (contains? (p/get-pressed-keys game) 38))
    (* -1 max-jump-velocity)
    :else
    y-velocity))

(defn get-direction
  [{:keys [x-velocity direction]}]
  (cond
    (> x-velocity 0) :right
    (< x-velocity 0) :left
    :else
    direction))

(defn touching-tile? [^js/p5.TiledMap tiled-map layer-index x y width height]
  (let [tile-size (.getTileSize tiled-map)
        start-x (int (/ x (.-x tile-size)))
        start-y (int (/ y (.-y tile-size)))
        end-x (inc (int (/ (+ x width) (.-x tile-size))))
        end-y (int (/ (+ y height) (.-y tile-size)))
        tiles (for [tile-x (range start-x end-x)
                    tile-y (range end-y start-y -1)]
                (.getTileIndex tiled-map layer-index tile-x tile-y))]
    (some? (first (filter pos? tiles)))))

