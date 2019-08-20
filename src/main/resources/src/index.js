import $ from "jquery";
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import './css/main.css';
import * as PIXI from 'pixi.js'
import { Viewport } from 'pixi-viewport'
import { Program } from './modules/Program';
import { Binder } from './modules/Binder';
import { navbar } from './modules/Navbar';

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


for (let i = 0; i < 10; i++) {
  let x = Math.floor(Math.random() * app.screen.width), y = Math.floor(Math.random() * app.screen.height);
  let program = new Program(x, y, 'C:/Privado/TFG/Arnion/target/classes/static/img/assets');
  mainContainer.addChild(program);
}

export { mainContainer }