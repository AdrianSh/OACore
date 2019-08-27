import * as PIXI from 'pixi.js'
import Button from './Button'
import { Process } from './Process'
import { mainContainer, app } from '../../index'
import { processProperties } from '../html/ProcessProperties';

class Navbar extends PIXI.Sprite {
    constructor() {
        let backgroundTexture = PIXI.Loader.shared.resources.tileset.texture.clone();
        backgroundTexture.frame = new PIXI.Rectangle(0, 200, 50, 800);
        backgroundTexture.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation
        super(backgroundTexture);

        this.x = 0;
        this.y = 0;
        this.width = 50;
        this.height = window.innerHeight;

        this.addProcessButton();

    }

    addProcessButton() {
        let textureButton = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButton.frame = new PIXI.Rectangle(0, 158, 39, 35);
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButtonOver.frame = new PIXI.Rectangle(40, 157, 39, 35);
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation


        let button = new Button(textureButton, textureButtonOver, textureButtonOver, 4, 3, 39, 34, () => {
            let mousePos = app.renderer.plugins.interaction.mouse.global;
            let pro = new Process(mousePos.x, mousePos.y, '');
            processProperties.process = pro;
            processProperties.show();
            pro.dragging = true;
            mainContainer.addChild(pro);
        });

        this.addChild(button);
    }
}

export { Navbar };