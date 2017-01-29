(ns functional-game-dev.core
  (:require [play-cljs.core :as p]
            [functional-game-dev.state :as s]
            [functional-game-dev.utils :as u]
            [functional-game-dev.time-travel :as tt]
            [goog.events :as events]))

(defonce game (p/create-game u/view-size u/view-size))
(defonce state (atom nil))
(defonce paused? (atom false))

(defn smiley []
  [:fill {:color "yellow"}
   [:ellipse {:width 100 :height 100}
    [:fill {:color "black"}
     [:ellipse {:x -20 :y -10 :width 10 :height 10}]
     [:ellipse {:x 20 :y -10 :width 10 :height 10}]]
    [:fill {}
     [:arc {:width 60 :height 60 :start 0 :stop 3.14}]]]])

(def raw-slides
  [[:text {:value "Functional Game\nDevelopment" :halign :center}]
   [[:text {:value "Popular libraries" :halign :center}]
    [:text {:value (str "Unity (C#)" \newline
                     "libGDX (Java)" \newline
                     "Phaser, p5.js (JavaScript)")
            :halign :center :size 16 :y 50}]]
   [[:text {:value "Use hosted languages!" :halign :center}]
    [:text {:value (str "Unity (Clojure, F#)" \newline
                     "libGDX (Clojure, Scala)" \newline
                     "Phaser, p5.js (ClojureScript, PureScript)")
            :halign :center :size 16 :y 50}]]
   [[:text {:value "Why functional programming?" :halign :center}]
    [:text {:value (str "Hot code reloading" \newline
                     "REPL-driven development" \newline
                     "Time travelling")
            :halign :center :size 16 :y 50}]]
   [[:text {:value "Procedural" :halign :center :y -50}]
    [:div {:x 200 :y 50}
     (smiley)]
    [:text {:value "fill(\"yellow\");
ellipse(50, 50, 100, 100);
fill(\"black\");
ellipse(30, 40, 10, 10);
ellipse(70, 40, 10, 10);
noFill();
arc(50, 55, 60, 60, 0, 3.14);"
            :font "Courier New" :halign :left :size 14 :x -150}]]
   [[:text {:value "Object-Oriented" :halign :center :y -50}]
    [:text {:value "var head = new Ellipse(100, 100);
head.setFill(\"yellow\");
head.add(new Ellipse(-20, -10, 10, 10));
head.add(new Ellipse(20, -10, 10, 10));
head.add(new Arc(60, 60, 0, 3.14));
var stage = new Stage();
stage.add(head);
stage.draw();"
            :font "Courier New" :halign :left :size 14 :x -150}]]
   [[:text {:value "Data-Oriented" :halign :center :y -50}]
    [:text {:value "var commands =
  [\"fill\", {color: \"yellow\"},
   [\"ellipse\", {width: 100, height: 100},
    [\"fill\", {color: \"black\"},
     [\"ellipse\", {x: -20, y: -10, width: 10, height: 10}],
     [\"ellipse\", {x: 20, y: -10, width: 10, height: 10}]],
    [\"fill\", {},
     [\"arc\", {width: 60, height: 60, start: 0, stop: 3.14}]]]];

render(commands);"
            :font "Courier New" :halign :left :size 14 :x -150}]]
   [:text {:value "(def commands
  [:fill {:color \"yellow\"}
   [:ellipse {:width 100 :height 100}
    [:fill {:color \"black\"}
     [:ellipse {:x -20 :y -10 :width 10 :height 10}]
     [:ellipse {:x 20 :y -10 :width 10 :height 10}]]
    [:fill {}
     [:arc {:width 60 :height 60 :start 0 :stop 3.14}]]]])

(render commands)"
            :font "Courier New" :halign :left :size 14 :y -50}]
   [[:div {:x 450 :y -50}
     [:text {:value "Try it out:" :halign :center}]
     [:text {:value (str "https://github.com/oakes/play-cljs")
             :halign :center :size 16 :y 50}]]]])

(def slides
  (reduce
    (fn [nested-slides slide]
      [:div {:x u/view-size} slide nested-slides])
    []
    (reverse raw-slides)))

(defn on-key-down [^js/KeyboardEvent event]
  (when (= (.-keyCode event) 80)
    (swap! paused? not)))

(def main-screen
  (reify p/Screen
    (on-show [_]
      (when-not @state
        (events/listen js/window "keydown" on-key-down)
        (reset! state (s/initial-state game))))
    (on-hide [_])
    (on-render [_]
      (let [{:keys [x y direction current]} @state
            koala-x (if (= direction :left) (- u/koala-offset) u/koala-offset)]
        (p/render game [[:stroke {}
                         [:fill {:color (if @paused? "gray" "lightblue")}
                          [:rect {:width u/view-size :height u/view-size}]]]
                        [:tiled-map {:value (:map @state) :x x}]
                        [:div {:x (- (+ x 350)) :y 100}
                         slides]
                        [:div {:x koala-x :y y}
                         current]]))
      (when-not @paused?
        (reset! state
          (or (tt/rewind game 1)
              (-> @state
                  (s/move game)
                  (s/prevent-move game)
                  (s/animate))))))))

(doto game
  (p/start)
  (p/set-screen main-screen))

(tt/start-recording state)

