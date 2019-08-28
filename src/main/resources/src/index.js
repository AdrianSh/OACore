import $ from "jquery";
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import './css/main.css';
import * as PIXI from 'pixi.js'
import { Viewport } from 'pixi-viewport'
import { Process } from './modules/PIXI/Process';
import { Binder } from './modules/PIXI/Binder';
import { Navbar } from './modules/PIXI/Navbar';
import tileset from './img/tileset.png';
import { navbar } from './modules/html/Navbar';
import { processProperties } from './modules/html/ProcessProperties';
import Server from './modules/Server';

let type = "WebGL"
if (!PIXI.utils.isWebGLSupported()) {
  type = "canvas"
}

PIXI.utils.sayHello(type);

const app = new PIXI.Application({
  width: window.innerWidth,
  height: window.innerHeight,
  autoDensity: true,
  backgroundColor: 0xF2F2F2,
  resizeTo: window
});

document.body.appendChild(app.view);

function loadProgressHandler() {
  console.log("loading");
}

const loader = PIXI.Loader.shared
loader
  .add("tileset", tileset)
  .on("progress", loadProgressHandler)
  .load(setup);

const viewport = new Viewport({ // create viewport
  screenWidth: window.innerWidth,
  screenHeight: window.innerHeight,
  worldWidth: 1000,
  worldHeight: 1000,
  interaction: app.renderer.plugins.interaction // the interaction module is important for wheel to work properly when renderer.view is placed or scaled
})

app.stage.addChild(viewport) // add the viewport to the stage

viewport // activate plugins
  .drag({ mouseButtons: 'middle-right' })
  .pinch()
  .wheel()
  .decelerate();

viewport.on('zoomed', (e) => {
  navbar.nav.hide();
})
viewport.on('drag-start', e => {
  navbar.nav.hide();
});

const mainContainer = new PIXI.Container();
viewport.addChild(mainContainer);

async function saveWorkflow() {
  let wData = {};
  wData['_id'] = app.workflowData._id;
  wData['userId'] = app.workflowData.userId;
  wData['executorServices'] = app.workflowData.executorServices;

  wData['process'] = [];
  await Object.getOwnPropertyNames(app.workflowData.processes).forEach(pId => {
    wData['process'].push({ '$oid': pId });
  });

  wData['binder'] = [];
  await Object.getOwnPropertyNames(app.workflowData.binders).forEach(bId => {
    wData['binder'].push({ '$oid': bId });
  });

  wData['objCoords'] = JSON.stringify(app.workflowData.objCoords);
  wData['binderProcessesOrig'] = JSON.stringify(app.workflowData.binderProcessesOrig);
  wData['binderProcessesDest'] = JSON.stringify(app.workflowData.binderProcessesDest);

  console.log(`Saving workflow... ${JSON.stringify(wData)}`);

  await Server.putToServer(wData, 'admin/generic/workflow', (data) => {
    console.log(`Saved! ${JSON.stringify(data)}`);
  }, (jqXHR, status, err) => {
    console.log(`Error while saving the workflow! ${err}`);
  });
}

async function setup() {
  console.log("Setup");
  app.stage.addChild(new Navbar());

  await Server.getFromServer((w, status, jqXHR) => {
    console.log(`Workflow data: ${JSON.stringify(w)}`);
    w.objCoords = JSON.parse(w.objCoords);
    w.binderProcessesOrig = JSON.parse(w.binderProcessesOrig);
    w.binderProcessesDest = JSON.parse(w.binderProcessesDest);
    w.processes = {};
    w.binders = {};

    for (let i = 0; i < w.process.length; i++) {
      const pId = w.process[i]['$oid'];
      if (pId != undefined) {
        let p = undefined;
        if (w.objCoords[pId] != undefined) {
          p = new Process(w.objCoords[pId].x, w.objCoords[pId].y, undefined, pId);
        } else {
          p = new Process(100, 100, undefined, pId);
        }
        w.processes[pId] = p;
        console.log(`Adding a process: '${pId}': '${p.commandLineText.text}'`);
        mainContainer.addChild(p);
      }
    }

    for (let i = 0; i < w.binder.length; i++) {
      const bId = w.binder[i]['$oid'];
      if (bId != undefined) {
        console.log(`Processing binder '${bId}': ${w.binderProcessesOrig[bId]}, ${w.binderProcessesDest[bId]} `);
        console.log(`'${w.binderProcessesOrig[bId]}': ${w.processes[w.binderProcessesOrig[bId]]}`);
        console.log(`'${w.binderProcessesDest[bId]}': ${w.processes[w.binderProcessesDest[bId]]}`);

        if (w.binderProcessesOrig[bId] != undefined && w.binderProcessesDest[bId] != undefined
          && w.binderProcessesOrig[bId] != 'null' && w.binderProcessesOrig[bId] != null
          && w.binderProcessesDest[bId] != 'null' && w.binderProcessesDest[bId] != null) {
          let p1 = w.processes[w.binderProcessesOrig[bId]], p2 = w.processes[w.binderProcessesDest[bId]], b = new Binder(p1, p2, bId);
          w.binders[bId] = b;
          p1.addInputBinder(b);
          p2.addOutputBinder(b);
          setTimeout(() => {
            console.log(`Adding a binder: ${b.binderData}`);
          }, 1000);
          
          mainContainer.addChild(b);
        }
      }
    }

    app.workflowData = w;
    window.workflowData = app.workflowData;
  }, `admin/generic/workflow`, undefined, (jqXHR, status, err) => {
    console.log(`Error while loading main workflow, ${err}`);
  });
}
export { mainContainer, app, saveWorkflow }