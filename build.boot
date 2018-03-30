(def project 'heckle)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [midje "1.9.1" :scope "test"]
                            [zilti/boot-midje "0.2.2-SNAPSHOT" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "The CLJ/CLJS validation library you deserve"
      :url         "https://github.com/nwallace/heckle"
      :scm         {:url "https://github.com/nwallace/heckle"}
      :license     {"MIT License"
                    "https://opensource.org/licenses/MIT"}})

(require '[zilti.boot-midje :refer [midje]])

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))
