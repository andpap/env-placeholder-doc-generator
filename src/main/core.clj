(ns core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]))

(defn yml-env-params
  [path]
  (let [data (slurp path)
        comment-prefix "#"
        type-prefix "type:"
        type-prefix-length (. type-prefix length)
        lines (map-indexed vector (s/split-lines data))
        lines-map (into {} lines)
        lines-with-placeholder (for [[index line] lines
                                     :when (s/includes? line "{")
                                     :let [values (re-seq #"\$\{[^}]+\}" line)]]
                                 [index (map #(s/replace % #"\$\{([^}]+)\}", "$1") values)])
        get-comment (fn [index]
                      (let [prev-line (s/trim (lines-map (dec index)))]
                        (if (s/starts-with? prev-line comment-prefix)
                          (subs prev-line (. comment-prefix length))
                          nil)))
        get-desc (fn [index]
                   (let [descriptions (loop [index index
                                             col '()]
                                        (let [comment (get-comment index)]
                                          (cond
                                            (nil? comment) col
                                            (s/starts-with? comment type-prefix) (recur (dec index) col)
                                            :else (recur (dec index) (conj col comment)))))]
                     (s/join descriptions)))
        get-type (fn [index]
                   (let [comment (get-comment index)]
                     (when (s/starts-with? (or comment "") type-prefix)
                       (s/trim (subs comment type-prefix-length)))))
        build-param (fn [value index & {:keys [append-type]}]
                      (let [[name default] (s/split value #":" 2)]
                        {:index index
                         :name name
                         :type (if append-type (or (get-type index) "") "")
                         :default (or default "")
                         :desc (get-desc index)}))]
    (flatten
      (for [[index values] lines-with-placeholder]
        (if (= 1 (count values))
          (build-param (first values) index :append-type true)
          (for [value values]
            (build-param value index)))))))

(defn generate-doc
  [path env-params]
  (let [_ (spit path "|Name|Type|Default|Description|\n")
        _ (spit path "|---|---|---|---|\n" :append true)]
    (doseq [{:keys [name type default desc]} env-params
            :let [line (format "| %s | %s | %s | %s |\n" name type default desc)]]
      (spit path line :append true))))

(defn generate-doc-from-and-to
  [path-in path-out]
  (let [params (yml-env-params path-in)]
    (generate-doc path-out params)))

(defn main [opts]
  (let [{:keys [path]} opts
        to-path (str path ".md")
        file (io/file path)]
    (if ( and
          (s/ends-with? path ".yml")
          (. file exists)
          (. file isFile))
      (do
        (generate-doc-from-and-to path to-path)
        (println "saved result to " to-path))
      (println "it's not a yml file"))))
