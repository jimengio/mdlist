
Docs
----

> This app is for grabbing markdown files in a folder and generate a webpage of documents. Currently it's hard-coded with the folder `/Users/chen/work/jimu/src/pkg.jimu.io` for out own project. Feel free to modify code and fit your situation.

### Usage

Launch in development mode:

```bash
yarn
yarn watch
node target/cli.js
yarn page
open http://localhost:7000
```

Bundle assets:

```bash
yarn
yarn shadow-cljs compile cli
node target/cli.js
yarn build
yarn http-server dist/
```

### Workflow

Workflow https://github.com/mvc-works/calcit-workflow

### License

MIT
