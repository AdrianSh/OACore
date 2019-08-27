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
import { Server } from './modules/Server';

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


function setup() {
  console.log("Setup");

  app.stage.addChild(new Navbar());

  let p1 = new Process(500,100, "P1")
  p1.processData = { "_id" : { "$oid" : "5d64ea860e075b0308154688" }, "command" : "null  ", "workingdirectory" : null, "modifiedEnvironment" : "null", "inheritIO" : false, "userId" : { "$oid" : "5d51a65023495b086cc04ec5" } };
  let p2 = new Process(500,500, "P2")
  p2.processData = { "_id" : { "$oid" : "5d64ea8a0e075b0308154689" }, "command" : "null  ", "workingdirectory" : null, "modifiedEnvironment" : "null", "inheritIO" : false, "userId" : { "$oid" : "5d51a65023495b086cc04ec5" } };
  let b1 = new Binder(p1, p2);
  b1.binderData = { "_id" : { "$oid" : "5d65053d0e075b03081546e5" }, "binderType" : "es.jovenesadventistas.arnion.process.binders.StdInBinder", "processId" : { "$oid" : "5d64ea8a0e075b0308154689" }, "stdInPublisherId" : { "$oid" : "5d65053d0e075b03081546e4" } };
  p1.addInputBinder(b1);
  p2.addOutputBinder(b1);

  mainContainer.addChild(p1);
  mainContainer.addChild(p2);
  mainContainer.addChild(b1);
}
export { mainContainer, app }