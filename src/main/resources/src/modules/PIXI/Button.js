import * as PIXI from 'pixi.js'

export default class Button extends PIXI.Sprite {
    constructor(textureButton, textureButtonDown, textureButtonOver, x, y, width, height, onClick = function(){ console.log(`Button clicked: ${this}`); }){
        super(textureButton);
        this.textureButton = textureButton;
        this.textureButtonDown = textureButtonDown;
        this.textureButtonOver = textureButtonOver;

        this.onClickFun = onClick;
        
        this.interactive = true;
        this.buttonMode = true;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.on('pointerdown', this.onButtonDown)
            .on('pointerup', this.onButtonUp)
            .on('pointerupoutside', this.onButtonUp)
            .on('pointerover', this.onButtonOver)
            .on('pointerout', this.onButtonOut)
            .on('pointerdown', this.onClickFun);
    }

    onButtonDown() {
        this.isdown = true;
        this.texture = this.textureButtonDown;
        this.alpha = 1;
    }
    
    onButtonUp() {
        this.isdown = false;
        if (this.isOver) {
            this.texture = this.textureButtonOver;
        }
        else {
            this.texture = this.textureButton;
        }
    }
    
    onButtonOver() {
        this.isOver = true;
        if (this.isdown) {
            return;
        }
        this.texture = this.textureButtonOver;
    }
    
    onButtonOut() {
        this.isOver = false;
        if (this.isdown) {
            return;
        }
        this.texture = this.textureButton;
    }
    
}