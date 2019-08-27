import * as PIXI from 'pixi.js';
import { Arrow } from './Arrow.js';
import { navbar } from '../html/Navbar';

export class Binder extends Arrow {
    constructor(firstProcess, secondProcess, lineColor = 0xc3c3c3, lineSize = 2) {
        super(lineColor, lineSize);
        this.origProcess = firstProcess;
        this.destProcess = secondProcess;
        this.hitArea = new PIXI.Polygon(new PIXI.Point(), new PIXI.Point(), new PIXI.Point(), new PIXI.Point());
        this.interactive = true;
        this.buttonMode = true;
        this.binderData = undefined;
        this.on('pointerdown', this.onDragStart);

        this.updatePoints();
    }

    destroy(){
        this.origProcess.binders.output.splice(this.origProcess.binders.output.indexOf(this));
        this.destProcess.binders.input.splice(this.origProcess.binders.input.indexOf(this));
        super.destroy();
    }

    _showNavbar(x, y){
        navbar.nav.css({ top: y, left: x, position: 'absolute' });
        navbar.process = undefined;
        navbar.binder = this;
        navbar.nav.show();
    }

    onDragStart(e){
        this._showNavbar(e.data.global.x, e.data.global.y);
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
        super.updatePoints([pOrig.x, pOrig.y, pDest.x, pDest.y]);
        this.hitArea.points = [pOrig.x - this.lineWidth, pOrig.y - this.lineWidth, pOrig.x + this.lineWidth, pOrig.y + this.lineWidth, 
            pDest.x - this.lineWidth, pDest.y - this.lineWidth, pDest.x + this.lineWidth, pDest.y + this.lineWidth];
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