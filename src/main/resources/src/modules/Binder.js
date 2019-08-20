import { Arrow } from './Arrow.js'

export class Binder extends Arrow {
    constructor(firstProgram, secondProgram, lineColor = 0xc3c3c3, lineSize = 2) {
        super(lineColor, lineSize);
        this.origProgram = firstProgram;
        this.destProgram = secondProgram;
        this.updatePoints();
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
        let pDest = this._bestPos(this.origProgram, this.destProgram, - this.triangle.length / 2, 2 + this.triangle.length / 2);
        let pOrig = this._bestPos(this.destProgram, this.origProgram, 0, 4);
        super.updatePoints([pOrig.x, pOrig.y, pDest.x, pDest.y]);
        return this;
    }

    updateDest() {
        let p = this._bestPos(this.origProgram, this.destProgram);
        super.updateDest(p.x, p.y);
        return this;
    }

    updateOrig() {
        let p = this._bestPos(this.destProgram, this.origProgram);
        super.updateOrig(p.x, p.y);
        return this;
    }
}