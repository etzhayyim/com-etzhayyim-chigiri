(ns chigiri.registry-seed-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def seed-path "registry/legal-aid.seed.edn")
(def seed (edn/read-string (slurp seed-path)))
(def referrals (get seed "referrals"))

(deftest registry-shape
  (is (seq referrals))
  (is (pos-int? (get seed "freshnessWindowDays"))))

(deftest referral-identities-are-complete-and-unique
  (let [ids (map #(get % "referralId") referrals)]
    (is (every? seq ids))
    (is (= (count ids) (count (set ids))))))

(deftest entries-ship-unverified
  (doseq [referral referrals]
    (is (= "unverified-seed" (get referral "verificationStatus"))
        (get referral "referralId"))))

(deftest provenance-and-verification-times-are-present
  (doseq [referral referrals]
    (testing (get referral "referralId")
      (is (str/starts-with? (str/trim (get referral "provenance" "")) "https://"))
      (is (seq (str/trim (get referral "lastVerified" "")))))))

(deftest registry-is-worldwide
  (is (every? #(seq (str/trim (get % "jurisdiction" ""))) referrals))
  (is (<= 12 (count (set (map #(get % "jurisdiction") referrals))))))

(deftest entries-reassert-the-upl-boundary
  (doseq [referral referrals]
    (let [notes (str/lower-case (get referral "notes" ""))]
      (is (str/includes? notes "upl") (get referral "referralId"))
      (is (str/includes? notes "no legal advice") (get referral "referralId"))))
  (let [corpus (str/lower-case (slurp seed-path))]
    (doseq [token ["upl" "referral" "no legal advice" "zero compensation"]]
      (is (str/includes? corpus token) token))))
