let siteUrl = `http://localhost:8080/static`;

let type = "WebGL"
if (!PIXI.utils.isWebGLSupported()) {
    type = "canvas"
}
PIXI.utils.sayHello(type);

const app = new PIXI.Application({
    width: window.innerWidth, 
    height: window.innerHeight,
    autoDensity: true,
    backgroundColor: 0xcccccc,
    resizeTo: window
});

document.body.appendChild(app.view);


// create viewport
const viewport = new Viewport.Viewport({
    screenWidth: window.innerWidth,
    screenHeight: window.innerHeight,
    worldWidth: 1000,
    worldHeight: 1000,

    interaction: app.renderer.plugins.interaction // the interaction module is important for wheel to work properly when renderer.view is placed or scaled
})

// add the viewport to the stage
app.stage.addChild(viewport)

// activate plugins
viewport
    .drag({mouseButtons: 'middle-right'})
    .pinch()
    .wheel()
    .decelerate();


let mainContainer = new PIXI.Container();
viewport.addChild(mainContainer);



const programTexture = PIXI.Texture.from(`${siteUrl}/img/assets/program.png`); // create a texture from an image path
programTexture.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

const basicTextstyle = new PIXI.TextStyle({
    fontFamily: 'Arial',
    fontSize: 11,
    fontWeight: 'bold',
    breakWords: true,
    wordWrap: true,
    wordWrapWidth: 240,
});




for (let i = 0; i < 10; i++) {
    createProgram(
        Math.floor(Math.random() * app.screen.width),
        Math.floor(Math.random() * app.screen.height),
    );
}

function createProgram(x, y, commandLine = 'C:/Privado/TFG/Arnion/target/classes/static/img/assets') {
    const program = new PIXI.Sprite(programTexture);
    program.interactive = true; // this will allow it to respond to mouse and touch events
    program.buttonMode = true; // this button mode will mean the hand cursor appears when you roll over the program with your mouse
    program.anchor.set(0.5); // center the program's anchor point
    // program.scale.set(3); // make it a bit bigger, so it's easier to grab

    // setup events for mouse + touch using the pointer events
    program
        .on('pointerdown', onDragStart)
        .on('pointerup', onDragEnd)
        .on('pointerupoutside', onDragEnd)
        .on('pointermove', onDragMove);

    // For mouse-only events
    // .on('mousedown', onDragStart)
    // .on('mouseup', onDragEnd)
    // .on('mouseupoutside', onDragEnd)
    // .on('mousemove', onDragMove);

    // For touch-only events
    // .on('touchstart', onDragStart)
    // .on('touchend', onDragEnd)
    // .on('touchendoutside', onDragEnd)
    // .on('touchmove', onDragMove);

    // move the sprite to its designated position
    program.x = x;
    program.y = y;

    let text = new PIXI.Text(commandLine.length > 45 ? '...' + commandLine.substr(commandLine.length - 42, 42) : commandLine, basicTextstyle);
    text.x = - 250/2 + 5 ; // corner top left + 5 width
    text.y = - 156/2 + 5; // corner top left + 5 height
    program.addChild(text);

    mainContainer.addChild(program);
}

function onDragStart(event) {
    // store a reference to the data
    // the reason for this is because of multitouch
    // we want to track the movement of this particular touch
    this.data = event.data;
    this.alpha = 0.5;
    this.dragging = true;
}

function onDragEnd() {
    this.alpha = 1;
    this.dragging = false;
    // set the interaction data to null
    this.data = null;
}

function onDragMove() {
    if (this.dragging) {
        const newPosition = this.data.getLocalPosition(this.parent);
        this.x = newPosition.x;
        this.y = newPosition.y;

        let collision = Intersects.boxBox();



        boxLine(xb, yb, wb, hb, x1, y1, x2, y2)
Box-line collision.

Param	Meaning
xb	top-left corner of box
yb	top-left corner of box
wb	width of box
hb	height of box
x1	first point of line
y1	first point of line
x2	second point of line
y2	second point of line


/*
LO QUE PUEDO HACER ES ALMACENAR LA LISTA DE TODOS LOS SPRITES 
y a lo que voy moviendo uno, mirar los de la posicion mas cercana y entonces fijarme en ellos para comprobar la colision.


(Una matriz de posiciones (array) en la que almacene los indices de los objetos que se encuentran ocupando esa posicion.... ¿Es más rapido?)
*/
    }
}
