import * as PIXI from 'pixi.js';
import { Arrow } from './Arrow.js';
import { navbar } from '../html/Navbar';
import Server from '../Server'
import { app, saveWorkflow } from '../../index'
import { Binder } from './Binder'
import Button from './Button.js';
import { binderProperties } from './../html/BinderProperties'

export class JoiningArrow extends Arrow {
    constructor(firstProcess, secondProcess, id1 = undefined, id2 = undefined, lineColor = 0xc3c3c3, lineSize = 2) {
        super(lineColor, lineSize);
        this.origProcess = firstProcess;
        this.destProcess = secondProcess;
        this.interactive = true;
        this.buttonMode = true;
        this.on('pointerdown', this.onDragStart);
        let callback = () => {
            binderProperties.process1 = this.origProcess;
            binderProperties.process2 = this.destProcess;
        };
        this.binderOrig = new Binder(0, 0, callback.bind(this), this.origProcess);
        this.binderDest = new Binder(0, 0, callback.bind(this), this.destProcess);

        let textureButton = PIXI.Loader.shared.resources.delete.texture.clone(),  textureButtonOver = PIXI.Loader.shared.resources.deleteOver.texture.clone();
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST;
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST;
        
        this.removeButton = new Button(textureButton, textureButtonOver, textureButtonOver, 0,0, 10, 10, () => {
            console.log(`Removing a Joining Arrow...`);
            this.destroy();
        });

        this.addChild(this.binderOrig);
        this.addChild(this.binderDest);
        this.addChild(this.removeButton);

        if (id1 != undefined) {
            Server.getFromServer((data) => {
                this.binderOrig.binderData = data;
                this.updatePoints();
                console.log(`Binder orig. loaded... ${JSON.stringify(data)}`);
            }, `admin/generic/binder/${id1}`, undefined, (xhr, status, err) => {
                console.error(`Couldn't load Binder ID: ${id1}, ${err}`);
            })
        }

        if (id2 != undefined) {
            Server.getFromServer((data) => {
                this.binderDest.binderData = data;
                this.updatePoints();
                console.log(`Binder dest. loaded... ${JSON.stringify(data)}`);
            }, `admin/generic/binder/${id2}`, undefined, (xhr, status, err) => {
                console.error(`Couldn't load Binder ID: ${id2}, ${err}`);
            })
        }

        this.updatePoints();
    }

    idOrig(){
        return this.binderOrig.binderData == undefined ? undefined : this._getId(this.binderOrig.binderData['_id']);
    }
    
    idDest(){
        return this.binderDest.binderData == undefined ? undefined : this._getId(this.binderDest.binderData['_id']);
    }

    _getId(id = undefined) {
        return id != undefined ? id['$oid'] : undefined;
    }

    async destroy() {
        if (this.idOrig() != undefined && this.idDest() != undefined) {
            await Server.deleteToServer(`admin/generic/binder/${this.idOrig()}`, (data, status, jqXHR) => {
                if (app.workflowData.binders[this.idOrig()] != undefined)
                    delete app.workflowData.binders[this.idOrig()];
                if (app.workflowData.binderProcessesOrig[this.idOrig()] != undefined)
                    delete app.workflowData.binderProcessesOrig[this.idOrig()];
                if (app.workflowData.binderProcessesDest[this.idOrig()] != undefined)
                    delete app.workflowData.binderProcessesDest[this.idOrig()];

                saveWorkflow();

            }, (jqXHR, status, err) => {
                console.log(`Couldn't delete Binder ${err}`);
            });

            await Server.deleteToServer(`admin/generic/binder/${this.idDest()}`, (data, status, jqXHR) => {
                if (app.workflowData.binders[this.idDest()] != undefined)
                    delete app.workflowData.binders[this.idDest()];
                if (app.workflowData.binderProcessesOrig[this.idDest()] != undefined)
                    delete app.workflowData.binderProcessesOrig[this.idDest()];
                if (app.workflowData.binderProcessesDest[this.idDest()] != undefined)
                    delete app.workflowData.binderProcessesDest[this.idDest()];

                saveWorkflow();
            }, (jqXHR, status, err) => {
                console.log(`Couldn't delete Binder ${err}`);
            });
        }

        this.origProcess.binders.output.splice(this.origProcess.binders.output.indexOf(this));
        this.destProcess.binders.input.splice(this.origProcess.binders.input.indexOf(this));
        super.destroy();
    }

    onDragStart(e) {
        console.log(`Binder IDs: ${this.idOrig()} -> ${this.idDest()}`);
        binderProperties.joiningArrow = this;
        binderProperties.process1 = this.origProcess;
        binderProperties.process2 = this.destProcess;
    }

    _bestPos(p1, p2, margin = 0, cornerMargin = 0) {
        let r = { x: p2.x, y: p2.y }, padding = [p2.width * p2.anchor._x, p2.height * p2.anchor._y];
        if (p1.x < p2.x - padding[0]) { // left
            r.x = p2.x - padding[0] + margin;
            if (p1.y < p2.y - padding[1]) { // up 
                r.x += cornerMargin;
                r.y = p2.y - padding[1] + margin + cornerMargin;
            } else if (p1.y > p2.y + padding[1]) { // down
                r.x += cornerMargin;
                r.y = p2.y + padding[1] - margin - cornerMargin;
            } // else center
        } else if (p1.x > p2.x + padding[0]) { // right
            r.x = p2.x + padding[0] - margin;
            if (p1.y < p2.y - padding[1]) { // up
                r.x -= cornerMargin;
                r.y = p2.y - padding[1] + margin + cornerMargin;
            } else if (p1.y > p2.y + padding[1]) { // down
                r.x -= cornerMargin;
                r.y = p2.y + padding[1] - margin - cornerMargin;
            } // else center
        } else { // center
            if (p1.y < p2.y - padding[1]) { // up 
                r.y = p2.y - padding[1] + margin;
            } else if (p1.y > p2.y + padding[1]) { // down
                r.y = p2.y + padding[1] - margin;
            } // else center
        }
        return r;
    }

    updatePoints() {
        let pDest = this._bestPos(this.origProcess, this.destProcess, - this.triangle.length / 2, 2 + this.triangle.length / 2);
        let pOrig = this._bestPos(this.destProcess, this.origProcess, 0, 4);
        this.binderOrig.x = pOrig.x;
        this.binderOrig.y = pOrig.y + 5;
        this.binderDest.x = pDest.x - 24;
        this.binderDest.y = pDest.y + 5;
        this.removeButton.x = pOrig.x + 24;
        this.removeButton.y = pOrig.y + 10;
        super.updatePoints([pOrig.x, pOrig.y, pDest.x, pDest.y]);
        return this;
    }

    updateDest() {
        let p = this._bestPos(this.origProcess, this.destProcess);
        super.updateDest(p.x, p.y);
        return this;
    }

    updateOrig() {
        let p = this._bestPos(this.destProcess, this.origProcess);
        super.updateOrig(p.x, p.y);
        return this;
    }
}