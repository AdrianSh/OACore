import * as PIXI from 'pixi.js'
import boxBox from 'intersects/box-box'
import img from '../img/program.png';
import { navbar } from './Navbar';

console.log(img);

const programTexture = PIXI.Texture.from(img); // create a texture from an image path
programTexture.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

const basicTextstyle = new PIXI.TextStyle({
    fontFamily: 'Arial',
    fontSize: 11,
    fill: "#FFFFFF",
    // fontWeight: 'bold',
    breakWords: true,
    wordWrap: true,
    wordWrapWidth: 240,
    dropShadow: true,
    dropShadowAngle: 0.3,
    dropShadowAlpha: 0.2,
    dropShadowDistance: 1,
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

        this.binders = { input: [], output: [] };

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

        this.moving = false;
    }

    destroy(){
        this.binders.input.forEach(b => { b.destroy() })
        this.binders.output.forEach(b => { b.destroy() })
        navbar.program = navbar.lastProgram = undefined;
        navbar.nav.hide();
        super.destroy();
    }

    upperCornerCoords() {
        return [this.x - 250 / 2, this.y - 156 / 2];
    }

    addInputBinder(binder) {
        this.binders.input.push(binder);
    }

    addOutputBinder(binder) {
        this.binders.output.push(binder);
    }

    onDragStart(event) {
        // store a reference to the data
        // the reason for this is because of multitouch
        // we want to track the movement of this particular touch
        this.draggingObjectData = event.data;
        this.alpha = 0.5;
        this.dragging = true;
        this._showNavbar();
    }

    _showNavbar(){
        let gPos = this.getGlobalPosition();
        navbar.nav.css({ top: gPos.y, left: gPos.x, position: 'absolute' });
        navbar.nav.show();
        navbar.program = this;
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

            if (this.binders.input.length > 0)
                this.binders.input.forEach(b => b.updatePoints());
            if (this.binders.output.length > 0)
                this.binders.output.forEach(b => b.updatePoints());

            this._showNavbar();

            let collision = boxBox();

            // console.log(`Moving to: ${JSON.stringify(newPosition)} from: (${this.x}, ${this.y})`);
            // en THIS TENEMOS EL OBJETO,   en position tenemos la posicion, luego width y height para calcular colisiones
        }
    }
}

export { Program };