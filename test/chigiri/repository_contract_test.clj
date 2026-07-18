(ns chigiri.repository-contract-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]))

(defn read-edn [path] (edn/read-string (slurp path)))

(deftest canonical-repository-documents
  (let [contract (read-edn "repository-contracts.edn")
        identity (read-edn "identity.edn")
        manifest (read-edn "manifest.edn")]
    (is (= :edn (:repository/canonical-format contract)))
    (is (= "chigiri" (:identity/actor identity)))
    (is (= "chigiri" (get manifest "name")))
    (doseq [legacy (:repository/legacy-artifacts-forbidden contract)]
      (is (not (.exists (io/file legacy))) (str legacy " must stay pruned")))))

(deftest actor-owned-data-is-edn-canonical
  (let [lexicons (filter #(and (.isFile %) (.endsWith (.getName %) ".edn"))
                         (file-seq (io/file "lex")))]
    (is (= 11 (count lexicons)))
    (doseq [lexicon lexicons]
      (is (map? (read-edn (.getPath lexicon))) (.getPath lexicon))))
  (is (map? (read-edn "registry/legal-aid.seed.edn"))))

(deftest dependencies-are-reproducibly-pinned
  (doseq [dependency (:dependencies (read-edn "dependencies.edn"))]
    (is (re-matches #"[0-9a-f]{40}" (:dependency/revision dependency))
        (str (:dependency/id dependency)))))
