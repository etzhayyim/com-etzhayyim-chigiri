(ns chigiri.murakumo
  "Pure cljc actor boundary generated from manifest migration scaffold."
  (:require [clojure.string :as str]))

(def actor-did
  "did:web:chigiri.etzhayyim.com")

(def common-gates
  [:council-charter-attestation
   :no-platform-held-key-baseline
   :no-probing-baseline
   :murakumo-only-inference-baseline
   :did-primary-baseline
   :append-only-gate-baseline
   :kotoba-only-substrate-baseline])

(defn collection
  [name]
  (str "com.etzhayyim.chigiri." name))

(def cell-specs {
  :charters_attestation {:legacy-cell "charters-attestation"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "charters_attestation")]
     :required-gates common-gates
     :trigger "manifest cell charters_attestation"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :council_procedure {:legacy-cell "council-procedure"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "council_procedure")]
     :required-gates common-gates
     :trigger "manifest cell council_procedure"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :member_onboarding {:legacy-cell "member-onboarding"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "member_onboarding")]
     :required-gates common-gates
     :trigger "manifest cell member_onboarding"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :member_offboarding {:legacy-cell "member-offboarding"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "member_offboarding")]
     :required-gates common-gates
     :trigger "manifest cell member_offboarding"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :covenant_ceremony {:legacy-cell "covenant-ceremony"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "covenant_ceremony")]
     :required-gates common-gates
     :trigger "manifest cell covenant_ceremony"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :inheritance {:legacy-cell "inheritance"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "inheritance")]
     :required-gates common-gates
     :trigger "manifest cell inheritance"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :dispute_mediation {:legacy-cell "dispute-mediation"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "dispute_mediation")]
     :required-gates common-gates
     :trigger "manifest cell dispute_mediation"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :ip_licensing {:legacy-cell "ip-licensing"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "ip_licensing")]
     :required-gates common-gates
     :trigger "manifest cell ip_licensing"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :tax_receipt {:legacy-cell "tax-receipt"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "tax_receipt")]
     :required-gates common-gates
     :trigger "manifest cell tax_receipt"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :employment_compliance {:legacy-cell "employment-compliance"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "employment_compliance")]
     :required-gates common-gates
     :trigger "manifest cell employment_compliance"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :data_privacy {:legacy-cell "data-privacy"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "data_privacy")]
     :required-gates common-gates
     :trigger "manifest cell data_privacy"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :transparent_force_authorization {:legacy-cell "transparent-force-authorization"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "transparent_force_authorization")]
     :required-gates common-gates
     :trigger "manifest cell transparent_force_authorization"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
  :legal_aid_clinic {:legacy-cell "legal-aid-clinic"
     :phase :event
     :murakumo-node "reuben"
     :collections [(collection "legal_aid_clinic")]
     :required-gates common-gates
     :trigger "manifest cell legal_aid_clinic"
     :ceiling "Manifest-driven migration scaffold; explicit execution stays in runtime methods"}
})

(defn safe-rkey
  [s]
  (let [clean (-> (str s)
                  (str/replace #"^did:web:" "")
                  (str/replace #"[^A-Za-z0-9._~-]" "-"))]
    (if (str/blank? clean) "unknown" clean)))

(defn gate-value
  [attestations gate]
  (or (get attestations gate)
      (get attestations (name gate))
      (when (set? attestations) (attestations gate))
      (when (set? attestations) (attestations (name gate)))))

(defn missing-gates
  [spec attestations]
  (->> (:required-gates spec)
       (remove #(boolean (gate-value attestations %)))
       vec))

(defn put-record-effect
  [collection rkey record]
  {:op :mst/put-record
   :actor actor-did
   :collection collection
   :rkey rkey
   :record record})

(defn records-for
  [spec {:keys [records record computed-at request-id]
         :as input}]
  (let [input-records (cond
                        (map? records) records
                        (some? record) {0 record}
                        :else {})
        base {:actorDid actor-did
              :computedAt computed-at
              :legacyCell (:legacy-cell spec)
              :phase (:phase spec)
              :requestId request-id
              :actorBoundary "cljc-migration-scaffold"
              :scaffold true
              :constitutionalStatus "attested-plan"}]
    (map-indexed
     (fn [idx coll]
       (let [record* (merge {:$type coll}
                            base
                            (or (get input-records coll)
                                (get input-records idx)
                                {}))
             rkey (safe-rkey (or (:rkey record*)
                                 (get record* "rkey")
                                 (:tid record*)
                                 request-id
                                 (str (:legacy-cell spec) "-" idx)))]
         {:collection coll
          :record record*
          :rkey rkey}))
     (:collections spec))))

(defn cell-plan
  [cell-key {:keys [attestations] :as input}]
  (let [spec (get cell-specs cell-key)]
    (when-not spec
      (throw (ex-info "unknown cell" {:cell cell-key})))
    (let [missing (missing-gates spec attestations)]
      (merge
       {:cell cell-key
        :legacy-cell (:legacy-cell spec)
        :actor actor-did
        :phase (:phase spec)
        :murakumo-node (:murakumo-node spec)
        :trigger (:trigger spec)
        :ceiling (:ceiling spec)
        :required-gates (:required-gates spec)
        :missing-gates missing}
       (if (seq missing)
         {:status :blocked
          :effects []}
         (let [planned-records (records-for spec input)]
           {:status :ready
            :records (vec planned-records)
            :effects (mapv (fn [{:keys [collection record rkey]}]
                             (put-record-effect collection rkey record))
                           planned-records)}))))))

(defn all-cell-plans
  [input]
  (into {}
        (map (fn [cell-key] [cell-key (cell-plan cell-key input)]))
        (keys cell-specs)))
