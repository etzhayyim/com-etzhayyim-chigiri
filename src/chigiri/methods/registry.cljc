#!/usr/bin/env bb
;; chigiri 契 — legal-aid REFERRAL registry loader (canonical EDN seed → normalized maps).
(ns chigiri.methods.registry
  "registry.cljc — chigiri 契 referral-registry loader.

  Reads the worldwide legal-aid REFERRAL registry (registry/legal-aid.seed.edn,
  ADR-2605262700) and normalizes each entry to a map of ':referral/*' STRING-keyword
  attributes — the shape the datom emitter + ledger consume. Order-preserving (the
  EDN `referrals` vector order is significant for deterministic EAVT emission).

  G8/G14 honesty: every entry codes a real public-interest legal-aid body / bar
  pro-bono program / court self-help center to which a consenting member may be
  ROUTED — a referral TARGET, never an advice source. chigiri renders NO legal advice
  (UPL). All seed entries are :unverified-seed; the loader preserves that status
  verbatim (it never upgrades verification)."
  (:require [clojure.edn :as edn]
            #?(:clj [clojure.java.io :as io])))

;; entry id → entity id (content-stable)
(defn entity-id [referral-id] (str "referral:" referral-id))

;; JSON key → datom attribute. Free-text `notes` is intentionally omitted (kept out of
;; the ledger to stay lean); everything else is wayfinding-relevant ground state.
(def attr-map
  (array-map
   "title"              ":referral/title"
   "jurisdiction"       ":referral/jurisdiction"
   "bloc"               ":referral/bloc"
   "authority"          ":referral/authority"
   "channel"            ":referral/channel"
   "legalBasis"         ":referral/legal-basis"
   "language"           ":referral/language"
   "provenance"         ":referral/provenance"
   "confidence"         ":referral/confidence"
   "lastVerified"       ":referral/last-verified"
   "verificationStatus" ":referral/verification-status"))

;; the datom attribute order (deterministic emission)
(def referral-attrs (vec (vals attr-map)))

(defn normalize
  "One raw EDN referral map → {:referral/id … :referral/title … } (string-keyword keys)."
  [raw]
  (reduce-kv (fn [m jk dk] (if (contains? raw jk) (assoc m dk (get raw jk)) m))
             {":referral/id" (get raw "referralId")}
             attr-map))

#?(:clj
   (def ^:private here-dir
     ;; captured at namespace-LOAD time (sci rebinds *file* to the caller / -e expr at
     ;; call time) — robust under the bb -e suite runner. Actor root = src/chigiri/methods/../../../.
     (-> *file* io/file .getCanonicalFile .getParentFile .getParentFile .getParentFile .getParentFile)))

#?(:clj
   (defn default-seed-path []
     (str (io/file here-dir "registry" "legal-aid.seed.edn"))))

#?(:clj
   (defn load-referrals
     "Vector of normalized referral maps from the EDN seed (default or given path),
     in registry order."
     ([] (load-referrals (default-seed-path)))
     ([path]
      (->> (get (edn/read-string (slurp path)) "referrals")
           (mapv normalize)))))
