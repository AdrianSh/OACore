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

const viewport = new Viewport.Viewport({ // create viewport
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



class Program extends PIXI.Sprite {
    constructor(x, y, commandLine) {
        super(programTexture);
        this.interactive = true; // this will allow it to respond to mouse and touch events
        this.buttonMode = true; // this button mode will mean the hand cursor appears when you roll over the program with your mouse
        this.anchor.set(0.5); // center the program's anchor point
        // this.scale.set(3); // make it a bit bigger, so it's easier to grab

        this.x = x;
        this.y = y;
        this.binders = {input: [], output: []};

        // Setup events for mouse + touch using the pointer events
        this.on('pointerdown', this.onDragStart)
            .on('pointerup', this.onDragEnd)
            .on('pointerupoutside', this.onDragEnd)
            .on('pointermove', this.onDragMove)
        /*
            .on('mousedown', onDragStart)
            .on('mouseup', onDragEnd)
            .on('mouseupoutside', onDragEnd)
            .on('mousemove', onDragMove)
            .on('touchstart', onDragStart)
            .on('touchend', onDragEnd)
            .on('touchendoutside', onDragEnd)
            .on('touchmove', onDragMove)
        */

       let commandLineText = new PIXI.Text(commandLine.length > 45 ? '...' + commandLine.substr(commandLine.length - 42, 42) : commandLine, basicTextstyle);
       commandLineText.x = - 250 / 2 + 5; // corner top left + 5 width
       commandLineText.y = - 156 / 2 + 5; // corner top left + 5 height
       this.addChild(commandLineText);
    }

    addInputBinder(binder){
        this.binders.input.push(binder);
    }

    addOutputBinder(binder){
        this.binders.output.push(binder);
    }

    onDragStart(event) {
        // store a reference to the data
        // the reason for this is because of multitouch
        // we want to track the movement of this particular touch
        this.draggingObjectData = event.data;
        this.alpha = 0.5;
        this.dragging = true;
    
        console.log(`Dragging... this and event data:`);
        console.log(this);
        console.log('------------------------');
        console.log(event.data);
    }
    
    onDragEnd() {
        this.alpha = 1;
        this.dragging = false;
        // set the interaction data to null
        this.draggingObjectData = null;
    }
    
    onDragMove() {
        if (this.dragging) {
            const newPosition = this.draggingObjectData.getLocalPosition(this.parent);
            this.x = newPosition.x;
            this.y = newPosition.y;
    
            if(this.binders.input.length > 0)
                this.binders.input.forEach(b => b.updateDest(this.x, this.y));
            if(this.binders.output.length > 0)
                this.binders.output.forEach(b => b.updateOrig(this.x, this.y));

            let collision = Intersects.boxBox();
    
            console.log(`Moving to: ${JSON.stringify(newPosition)} from: (${this.x}, ${this.y})`);
            // en THIS TENEMOS EL OBJETO,   en position tenemos la posicion, luego width y height para calcular colisiones
        }
    }
}

class Line extends PIXI.Graphics {
    constructor(points, lineSize, lineColor) {
        super();
        this.lineWidth = lineSize || 5;
        this.lineColor = lineColor || "0x000000";
        this.points = points;
        this.lineStyle(this.lineWidth, this.lineColor)
        this.moveTo(points[0], points[1]);
        this.lineTo(points[2], points[3]);
    }
    
    updatePoints(p) {
        this.points = p.map((val, index) => val || this.points[index]);
        this.clear();
        this.lineStyle(this.lineWidth, this.lineColor);
        this.moveTo(this.points[0], this.points[1]);
        this.lineTo(this.points[2], this.points[3]);
    }

    updateDest(x, y){
        this.clear();
        this.lineStyle(this.lineWidth, this.lineColor);
        this.moveTo(this.points[0], this.points[1]);
        this.points[2] = x; this.points[3] = y;
        this.lineTo(this.points[2], this.points[3]);
    }

    updateOrig(x, y){
        this.clear();
        this.lineStyle(this.lineWidth, this.lineColor);
        this.points[0] = x; this.points[1] = y;
        this.moveTo(this.points[0], this.points[1]);
        this.lineTo(this.points[2], this.points[3]);
    }
}

let lastProgram = {};
for (let i = 0; i < 10; i++) {
    let x = Math.floor(Math.random() * app.screen.width), y = Math.floor(Math.random() * app.screen.height);
    let program = new Program(x, y, 'C:/Privado/TFG/Arnion/target/classes/static/img/assets');
    if(i > 1){
        let line = new Line([lastProgram.x, lastProgram.y, x, y]);
        mainContainer.addChild(line);
        program.addInputBinder(line);
        lastProgram.p.addOutputBinder(line);
    }
    lastProgram = { p : program, x : x, y: y};
    
    mainContainer.addChild(program);

}
