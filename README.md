# monotonic

Stop putting UUIDs and Mongo ids all over your urls. Monotonic is a simple service that promises to give you strictly monotonically increasing sequences. Every request for ids is guaranteed to give you some bigger than the ones given to you before. It's just like MySQL autoincrement columns or actually a bit more like Oracle sequences if you swing that way. Even if you're using MySQL with monotonic you can create an id scheme that spans tables.

See this in action at http://monotonic.io/

# Install it yourself

First you'll need to install redis, clojure and leiningen. Then you can grab this repository and run:

```lein ring server```


