# word-counter

Word counter is a reagent/ring clojure word counting service. If you give it a URL, it'll count the words on that page.

## Dependencies

* [leiningen](https://github.com/technomancy/leiningen)

## Running

To run, you should be able to just run ```lein cljsbuild once && lein ring server-headless``` . This will pull down any necessary dependencies, build the clojurescript, and run an http server. It should run on port 3000.

To view the app, you should be able to just hit [http://localhost:3000](http://localhost:3000).

## Running Tests
You can also run the unit tests via ```lein test```

## Notes

The app was tested with firefox and chrome, but not IE, as I do not have a windows machine. :)

### Rationale

A couple of notes on this implementation. 

* I decided not to use a database in the backend. Each time you restart the service, it'll restart its history. The reason for this is that I wanted to make it as simple as possible to run. I didn't want to require a database for such a simple process.
* The frontend is done in reagent, which is built on top of react. This makes  it easier to test the backend (no templating), and makes it easier for the frontend to have dynamic behavior.
* If I had more time to spend on this, probably the next steps would be improving the error behavior, and loading state. Right now it has a simple regex validation, and will show an error notification without much help. Also, it'd be nice to have a spinner while it's loading.
