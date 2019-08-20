import * as PIXI from 'pixi.js'

export class Triangle extends PIXI.Graphics {
    constructor(color = 0x709FE9, length = 10) {
        super();
        this.color = color;
        this.length = length;
    }

    updatePoints(cX, cY) {
        this.clear();
        let x = cX - this.length / 2, y = cY - this.length;
        this.beginFill(this.color, 1);
        this.lineStyle(0, this.color);
        this.moveTo(x, y);
        this.lineTo(x + this.length, y + this.length);
        this.lineTo(x, y + 2 * this.length);
        this.closePath();
        this.endFill();
    }
}