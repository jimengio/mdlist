
Docs
----

> This app is for grabbing markdown files in a folder and generate a webpage of documents. Currently it's hard-coded with the folder `/Users/chen/work/jimu/src/pkg.jimu.io` for out own project. Feel free to modify code and fit your situation.

### Usage

```bash
git clone $YOUR_REPO api-folder
```

Launch in development mode:

```bash
yarn
yarn watch
node target/cli.js # need to exit
yarn page
open http://localhost:7000
```

Bundle assets:

```bash
yarn
yarn shadow-cljs release cli
node dist/cli.js
yarn build
yarn http-server dist/
```

To update:

```bash
yarn up # pull API commits and rebuild site
yarn up-all # clear dist, build CLI, and do `yarn up`
```

### Workflow

Workflow https://github.com/mvc-works/calcit-workflow

### License

MIT
