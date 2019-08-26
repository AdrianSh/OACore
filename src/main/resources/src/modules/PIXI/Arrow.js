import * as PIXI from 'pixi.js'
import { Line } from './Line'
import { Triangle } from './Triangle'

export class Arrow extends Line {
    constructor(lineColor = 0xc3c3c3, lineSize = 1) {
        super([], lineSize, lineColor);
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