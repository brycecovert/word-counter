# word-counter

Word counter is a reagent/ring clojure word counting service. If you give it a URL, it'll count the words on that page.

## Running
Dependencies:
* leiningen

To run, you should be able to just do ```lein cljsbuild once && lein ring server-headless``` . This will pull down any necessary dependencies, build the clojurescript, and run an http server. It should run on port 3000.

To view the app, you should be able to just hit http://localhost:3000.
