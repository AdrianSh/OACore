import * as PIXI from 'pixi.js'

export class Line extends PIXI.Graphics {
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

    updateDest(x, y) {
        this.clear();
        this.lineStyle(this.lineWidth, this.lineColor);
        this.moveTo(this.points[0], this.points[1]);
        this.points[2] = x; this.points[3] = y;
        this.lineTo(this.points[2], this.points[3]);
    }

    updateOrig(x, y) {
        this.clear();
        this.lineStyle(this.lineWidth, this.lineColor);
        this.points[0] = x; this.points[1] = y;
        this.moveTo(this.points[0], this.points[1]);
        this.lineTo(this.points[2], this.points[3]);
    }
}