(require '[clojure.test :as t])

(doseq [ns-sym '[chigiri.methods.test-charter-gates
                  chigiri.methods.test-datom-kotoba
                  chigiri.methods.test-dispute-mediation
                  chigiri.registry-seed-test
                  chigiri.murakumo-test
                  chigiri.repository-contract-test]]
  (require ns-sym))

(let [result (apply t/run-tests
                    '[chigiri.methods.test-charter-gates
                      chigiri.methods.test-datom-kotoba
                      chigiri.methods.test-dispute-mediation
                      chigiri.registry-seed-test
                      chigiri.murakumo-test
                      chigiri.repository-contract-test])]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
