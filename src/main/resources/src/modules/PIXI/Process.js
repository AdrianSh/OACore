import * as PIXI from 'pixi.js'
import boxBox from 'intersects/box-box'
import { navbar } from '../html/Navbar';
import Server from '../Server'
import { app, saveWorkflow } from '../../index'

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

class Process extends PIXI.Sprite {
    constructor(x, y, commandLine, id = undefined) {
        let processTexture = PIXI.Loader.shared.resources.tileset.texture.clone();
        processTexture.frame = new PIXI.Rectangle(0, 0, 250, 156);
        processTexture.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        super(processTexture);

        if (id != undefined) {
            commandLine = 'Loading...';
            Server.getFromServer((data) => {
                this.processData = data;
                this.updateCommandLineText(data.command.trim());
                console.log(`Process loaded... ${JSON.stringify(data)}`);
            }, `admin/generic/AProcess/${id}`, undefined, (xhr, status, err) => {
                console.error(`Couldn't load AProcess ID: ${id}, ${err}`);
            })
        } else {
            this.processData = undefined;
        }

        this.interactive = true; // this will allow it to respond to mouse and touch events
        this.buttonMode = true; // this button mode will mean the hand cursor appears when you roll over the process with your mouse
        this.anchor.set(0.5); // center the process's anchor point
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

        this.commandLineText = new PIXI.Text(commandLine.length > 45 ? '...' + commandLine.substr(commandLine.length - 42, 42) : commandLine, basicTextstyle);
        this.commandLineText.x = - this.width * this.anchor._x + 5; // corner top left + 5 width
        this.commandLineText.y = - this.height * this.anchor._y + 5; // corner top left + 5 height
        this.addChild(this.commandLineText);

        this.moving = false;
    }

    updateCommandLineText(commandLine) {
        this.commandLineText.text = commandLine.length > 45 ? '...' + commandLine.substr(commandLine.length - 42, 42 - 1) : commandLine;
    }

    async destroy() {
        if (this.processData != undefined && this.processData._id != undefined && this.processData._id['$oid'] != undefined) {
            let pId = this.processData._id['$oid'];
            await Server.deleteToServer(`admin/generic/AProcess/${pId}`, (data, status, jqXHR) => {
                this.binders.input.forEach(b => { b.destroy() })
                this.binders.output.forEach(b => { b.destroy() })
                navbar.process = navbar.lastProcess = undefined;
                navbar.nav.hide();

                if (app.workflowData.processes[pId] != undefined)
                    delete app.workflowData.processes[pId];
                saveWorkflow();

                super.destroy();
            }, (jqXHR, status, err) => {
                console.log(`Couldn't delete the Process ${err}`);
            });
        } else {
            this.binders.input.forEach(b => { b.destroy() })
            this.binders.output.forEach(b => { b.destroy() })
            navbar.process = navbar.lastProcess = undefined;
            navbar.nav.hide();
            try { super.destroy(); } catch(e){ console.warn(`An error ocurred when destroying the process: ${e}`); }
        }
    }

    upperCornerCoords() {
        return [this.x - 250 / 2, this.y - 156 / 2];
    }

    addInputBinder(binder) {
        this.binders.output.push(binder);
    }

    addOutputBinder(binder) {
        this.binders.input.push(binder);
    }

    _showNavbar(x, y) {
        navbar.nav.css({ top: y, left: x, position: 'absolute' });
        navbar.nav.show();
        navbar.process = this;
        navbar.binder = undefined;
    }

    onDragStart(event) {
        // store a reference to the data
        // the reason for this is because of multitouch
        // we want to track the movement of this particular touch
        this.draggingObjectData = event.data;
        this.alpha = 0.5;
        this.dragging = true;
        this._showNavbar(event.data.global.x, event.data.global.y);
        console.log(`Program ID: ${this._getId()}`);
    }

    onDragEnd() {
        if (this.dragging) {
            saveWorkflow();
        }

        this.alpha = 1;
        this.dragging = false;
        // set the interaction data to null
        this.draggingObjectData = null;

    }

    _getId(id = undefined) {
        if (id == undefined)
            id = this.processData != undefined ? this.processData._id : { '$oid': undefined };
        return id['$oid'];
    }

    onDragMove(e) {
        if (this.dragging) {
            try {
                const newPosition = this.draggingObjectData.getLocalPosition(this.parent);
                this.x = newPosition.x;
                this.y = newPosition.y;

                if (this.binders.input.length > 0)
                    this.binders.input.forEach(b => b.updatePoints());
                if (this.binders.output.length > 0)
                    this.binders.output.forEach(b => b.updatePoints());

                if (app.workflowData != undefined && this._getId() != undefined) {
                    let c = app.workflowData.objCoords[this._getId()];
                    if (c == undefined) {
                        app.workflowData.objCoords[this._getId()] = { x: newPosition.x, y: newPosition.y };
                    } else {
                        c.x = newPosition.x;
                        c.y = newPosition.y;
                    }
                }

                this._showNavbar(e.data.global.x, e.data.global.y);
            } catch (err) {
                if (this.draggingObjectData == undefined || this.draggingObjectData == null)
                    this.draggingObjectData = e.data;
            }
        }
    }
}

export { Process };