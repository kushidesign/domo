# Domo quickstart

This is simple browser app with many examples of functions in the domo core
library. It is based on the Shadow CLJS quickstart browser template found [here](https://github.com/shadow-cljs/quickstart-browser).

You can uncomment and play with the various calls in the `src.main.starter.browser/start` function, to get of sense of Domo's API. There some messages and values printed to
browser dev console that describe what is going on.

## Required Software

- [node.js (v6.0.0+)](https://nodejs.org/en/download/)
- [Java JDK (8+)](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or [Open JDK (8+)](http://jdk.java.net/10/)

## Running

```bash
npm install
npx shadow-cljs watch app
```

This will begin the compilation of the configured `:app` build and re-compile whenever you change a file.

When you see a "Build completed." message your build is ready to be used.

```txt
[:app] Build completed. (23 files, 4 compiled, 0 warnings, 7.41s)
```

You can now then open [http://localhost:8020](http://localhost:8020).


