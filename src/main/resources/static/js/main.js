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
    backgroundColor: 0xF2F2F2,
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
        this.width = 250;
        this.height = 156;

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
       commandLineText.x = - this.width * this.anchor._x + 5; // corner top left + 5 width
       commandLineText.y = - this.height * this.anchor._y + 5; // corner top left + 5 height
       this.addChild(commandLineText);
    }

    upperCornerCoords(){
        return [ this.x - 250/2, this.y - 156/2];
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
                this.binders.input.forEach(b => b.updatePoints());
            if(this.binders.output.length > 0)
                this.binders.output.forEach(b => b.updatePoints());

            

            let collision = Intersects.boxBox();
    
            console.log(`Moving to: ${JSON.stringify(newPosition)} from: (${this.x}, ${this.y})`);
            // en THIS TENEMOS EL OBJETO,   en position tenemos la posicion, luego width y height para calcular colisiones
        }
    }
}

class Line extends PIXI.Graphics {
    constructor(points = [], lineSize = 1, lineColor = "0xc3c3c3") {
        super();
        this.lineWidth = lineSize;
        this.lineColor = lineColor;
        this.points = points;
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

class Triangle extends PIXI.Graphics {
    constructor(color = 0x709FE9, length = 10){
        super();
        this.color = color;
        this.length = length;
    }

    updatePoints(cX, cY) {
        this.clear();
        let x = cX - this.length / 2, y = cY - this.length;
        this.beginFill(this.color, 1);
        this.lineStyle(0, this.color);
        this.moveTo(x,y);
        this.lineTo(x + this.length, y + this.length);
        this.lineTo(x, y + 2 * this.length);
        this.closePath();
        this.endFill();
    }
}

class Arrow extends Line {
    constructor(lineColor = 0xc3c3c3, lineSize = 1){
        super();
        this.triangleContainer = new PIXI.Container();
        this.triangle = new Triangle(lineColor, lineSize + 4);
        this.triangleContainer.addChild(this.triangle);
        this.addChild(this.triangleContainer);
    }

    updatePoints(p) {
        super.updatePoints(p);
        this.triangleContainer.position.set(p[2], p[3]);
        this.triangleContainer.pivot.set(p[2], p[3]);
        this.triangle.updatePoints(p[2], p[3]);
        this.triangleContainer.rotation = Math.atan2(p[1] - p[3], p[0] - p[2]) + Math.PI;
    }
}

class Binder extends Arrow {
    constructor(firstProgram, secondProgram, lineColor = 0xc3c3c3, lineSize = 2) {
        super(lineColor, lineSize);
        this.origProgram = firstProgram;
        this.destProgram = secondProgram;
        this.updatePoints();
    }
    
    _bestPos(p1, p2, margin = 0, cornerMargin = 0){
        let r = { x: p2.x, y: p2.y}, padding = [p2.width * p2.anchor._x, p2.height * p2.anchor._y];
        if(p1.x < p2.x - padding[0]){ // left
            r.x = p2.x - padding[0] + margin;
            if(p1.y < p2.y - padding[1]){ // up 
                r.x += cornerMargin;
                r.y = p2.y - padding[1] + margin + cornerMargin;
            } else if(p1.y > p2.y + padding[1]) { // down
                r.x += cornerMargin;
                r.y = p2.y + padding[1] - margin - cornerMargin;
            } // else center
        } else if(p1.x > p2.x + padding[0]) { // right
            r.x = p2.x + padding[0] - margin;
            if(p1.y < p2.y - padding[1]){ // up
                r.x -= cornerMargin;
                r.y = p2.y - padding[1] + margin + cornerMargin;
            } else if(p1.y > p2.y + padding[1]) { // down
                r.x -= cornerMargin;
                r.y = p2.y + padding[1] - margin - cornerMargin;
            } // else center
        } else { // center
            if(p1.y < p2.y - padding[1]){ // up 
                r.y = p2.y - padding[1] + margin;
            } else if(p1.y > p2.y + padding[1]) { // down
                r.y = p2.y + padding[1] - margin;
            } // else center
        }
        return r;
    }

    updatePoints(){
        let pDest = this._bestPos(this.origProgram, this.destProgram, - this.triangle.length/2, 2 + this.triangle.length/2);
        let pOrig = this._bestPos(this.destProgram, this.origProgram, 0, 4);
        super.updatePoints([pOrig.x, pOrig.y, pDest.x, pDest.y]);
        return this;
    }

    updateDest(){
        let p = this._bestPos(this.origProgram, this.destProgram);
        super.updateDest(p.x, p.y);
        return this;
    }

    updateOrig(){
        let p = this._bestPos(this.destProgram, this.origProgram);
        super.updateOrig(p.x, p.y);
        return this;
    }
}

let lastProgram = {};
for (let i = 0; i < 10; i++) {
    let x = Math.floor(Math.random() * app.screen.width), y = Math.floor(Math.random() * app.screen.height);
    let program = new Program(x, y, 'C:/Privado/TFG/Arnion/target/classes/static/img/assets');
    if(i > 0){
        let b = new Binder(lastProgram.p, program);
        mainContainer.addChild(b);
        program.addInputBinder(b);
        lastProgram.p.addOutputBinder(b);
        // b.updateDest().updateOrig();
    }
    lastProgram = { p : program, x : x, y: y};
    
    mainContainer.addChild(program);

}
