(def project 'heckle)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [midje "1.9.1" :scope "test"]
                            [zilti/boot-midje "0.2.2-SNAPSHOT" :scope "test"]
                            [adzerk/bootlaces "0.1.13" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "The Clojure(Script) validation library you deserve"
      :url         "https://github.com/nwallace/heckle"
      :scm         {:url "https://github.com/nwallace/heckle"}
      :license     {"MIT License"
                    "https://opensource.org/licenses/MIT"}})

(require '[zilti.boot-midje :refer [midje]]
         '[adzerk.bootlaces :refer :all])

(bootlaces! version)

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))
