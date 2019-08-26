import $ from "jquery";
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import './css/main.css';
import * as PIXI from 'pixi.js'
import { Viewport } from 'pixi-viewport'
import { Program } from './modules/PIXI/Program';
import { Binder } from './modules/PIXI/Binder';
import { Navbar } from './modules/PIXI/Navbar';
import tileset from './img/tileset.png';
import { navbar } from './modules/html/Navbar';
import { programProperties } from './modules/html/ProgramProperties';
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


}
export { mainContainer, app }