(ns cljs-karaoke.subs.audio
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::audio-data
 (fn [db _]
   (:audio-data db)))

(defn reg-audio-data-sub [sub-name attr-name]
  (rf/reg-sub
   sub-name
   :<- [::audio-data]
   (fn [data _]
     (get data attr-name))))

(reg-audio-data-sub ::feedback-reduction? :feedback-reduction?)
(reg-audio-data-sub ::reverb-buffer :reverb-buffer)
(reg-audio-data-sub ::dry-gain :dry-gain)
(reg-audio-data-sub ::wet-gain :wet-gain)
(reg-audio-data-sub ::effect-input :effect-input)
(reg-audio-data-sub ::output-mix :output-mix)
(reg-audio-data-sub ::audio-input :audio-input)
(reg-audio-data-sub ::lp-input-filter :lp-input-filter)
(reg-audio-data-sub ::clean-analysesr :clean-analyser)
(reg-audio-data-sub ::reverb-analyser :reverb-analyser)
(reg-audio-data-sub ::freq-data :freq-data)
(reg-audio-data-sub ::audio-context :audio-context)

(rf/reg-sub
 ::microphone-enabled?
 :<- [::output-mix]
 (fn [mix _]
   (not (nil? mix))))