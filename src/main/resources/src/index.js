import $ from "jquery";
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import './css/main.css';
import * as PIXI from 'pixi.js'
import { Viewport } from 'pixi-viewport'
import { Process } from './modules/PIXI/Process';
import { JoiningArrow } from './modules/PIXI/JoiningArrow';
import { Navbar } from './modules/PIXI/Navbar';
import tilesetImg from './img/tileset.png';
import cookerImg from './img/icons8-cooker-48.png';
import cookerOverImg from './img/icons8-cooker-48-over.png';
import gearImg from './img/icons8-gear-48.png';
import gearOverImg from './img/icons8-gear-48-over.png';
import navigationImg from './img/icons8-navigation-toolbar-top-48.png';
import navigationOverImg from './img/icons8-navigation-toolbar-top-48-over.png';
import playImg from './img/icons8-play-48.png';
import playOverImg from './img/icons8-play-48-over.png';
import deleteImg from './img/icons8-delete-48.png';
import deleteOverImg from './img/icons8-delete-48-over.png';
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
  .add("tileset", tilesetImg)
  .add("cooker", cookerImg)
  .add("cookerOver", cookerOverImg)
  .add("gear", gearImg)
  .add("gearOver", gearOverImg)
  .add("navigation", navigationImg)
  .add("navigationOver", navigationOverImg)
  .add("play", playImg)
  .add("playOver", playOverImg)
  .add("delete", deleteImg)
  .add("deleteOver", deleteOverImg)
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
  wData['executorAssigned'] = JSON.stringify(app.workflowData.executorAssigned);

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
    w.executorAssigned = JSON.parse(w.executorAssigned);

    if (w.executorAssigned == null || w.executorAssigned == "null")
      w.executorAssigned = {};

    if (w.executorServices == null || w.executorServices == 'null')
      w.executorServices = ['java.util.concurrent.Executors$FinalizableDelegatedExecutorService'];

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

    /**
     * Build a matrix with ProcessID, ProcessID, BinderIds dim.
     */
    let tmpJoiningStructure = {};
    for (let i = 0; i < w.binder.length; i++) {
      const bId = w.binder[i]['$oid'];
      let pIdOrig = w.binderProcessesOrig[bId] != undefined && w.binderProcessesOrig[bId] != 'null' && w.binderProcessesOrig[bId] != null ? w.binderProcessesOrig[bId] : undefined;
      let pIdDest = w.binderProcessesDest[bId] != undefined && w.binderProcessesDest[bId] != 'null' && w.binderProcessesDest[bId] != null ? w.binderProcessesDest[bId] : undefined;

      console.log(`Temporal Joining Structure: '${bId}':   '${pIdOrig}', '${pIdDest}'`);

      if (pIdOrig != undefined && pIdDest != undefined) {
        if (tmpJoiningStructure[pIdOrig] == undefined) {
          tmpJoiningStructure[pIdOrig] = {};
          tmpJoiningStructure[pIdOrig][pIdDest] = [bId];
        } else {
          if (tmpJoiningStructure[pIdOrig][pIdDest] == undefined)
            tmpJoiningStructure[pIdOrig][pIdDest] = [bId];
          else
            tmpJoiningStructure[pIdOrig][pIdDest].push(bId);
        }


        if (tmpJoiningStructure[pIdDest] == undefined) {
          tmpJoiningStructure[pIdDest] = {};
          tmpJoiningStructure[pIdDest][pIdOrig] = [bId];
        } else {
          if (tmpJoiningStructure[pIdDest][pIdOrig] == undefined)
            tmpJoiningStructure[pIdDest][pIdOrig] = [bId];
          else
            tmpJoiningStructure[pIdDest][pIdOrig].push(bId);
        }
      }
    }

    console.log(tmpJoiningStructure);

    for (let i = 0; i < w.binder.length; i++) {
      const bId = w.binder[i]['$oid'];
      let pIdOrig = w.binderProcessesOrig[bId] != undefined && w.binderProcessesOrig[bId] != 'null' && w.binderProcessesOrig[bId] != null ? w.binderProcessesOrig[bId] : undefined;
      let pIdDest = w.binderProcessesDest[bId] != undefined && w.binderProcessesDest[bId] != 'null' && w.binderProcessesDest[bId] != null ? w.binderProcessesDest[bId] : undefined;

      if (bId != undefined && pIdOrig != undefined && pIdDest != undefined) {
        console.log(`Processing binder '${bId}': ${w.binderProcessesOrig[bId]}, ${w.binderProcessesDest[bId]}`);
        let p1 = w.processes[pIdOrig], p2 = w.processes[pIdDest], binders = tmpJoiningStructure[pIdOrig][pIdDest];
        let bId1 = binders[0], bId2 = binders[1];

        if (binders[2] == undefined && (bId1 != undefined || bId2 != undefined)) {
          let b = new JoiningArrow(p1, p2, bId1, bId2);

          w.binders[bId1] = b.binderOrig;
          if (bId2 != undefined)
            w.binders[bId2] = b.binderDest;

          p1.addInputBinder(b);
          p2.addOutputBinder(b);

          setTimeout(() => {
            console.log(`Adding the Joining Arrow for binders: ${bId1}, ${bId2}`);
          }, 1000);

          mainContainer.addChild(b);
          tmpJoiningStructure[pIdOrig][pIdDest][2] = tmpJoiningStructure[pIdDest][pIdOrig][2] = true;
        } else {
          console.log(`Those binders has been added. ${JSON.stringify(binders)}`);
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