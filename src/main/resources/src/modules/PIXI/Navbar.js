import * as PIXI from 'pixi.js'
import Button from './Button'
import { Process } from './Process'
import { mainContainer, app, saveWorkflow } from '../../index'
import { processProperties } from '../html/ProcessProperties';
import Server from './../Server';

class Navbar extends PIXI.Sprite {
    constructor() {
        let backgroundTexture = PIXI.Loader.shared.resources.tileset.texture.clone();
        backgroundTexture.frame = new PIXI.Rectangle(0, 200, 50, 800);
        backgroundTexture.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation
        super(backgroundTexture);

        this.width = 58;
        this.height = 159;
        this.buttonSize = { w: 40, h: 40 * (this.height / this.width) + ( this.height - this.width)};
        this.buttonMargin = 5;
        this.x = 0;
        this.y = 0;
        
        this.zIndex = -1;

        this.addProcessButton();
        this.addExecutorButton();
        this.addExecuteButton();
    }

    addProcessButton() {
        let textureButton = PIXI.Loader.shared.resources.navigation.texture.clone();
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.navigationOver.texture.clone();
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let button = new Button(textureButton, textureButtonOver, textureButtonOver, this.buttonMargin, this.buttonMargin, this.buttonSize.w, this.buttonSize.h, () => {
            let mousePos = app.renderer.plugins.interaction.mouse.global;
            let pro = new Process(mousePos.x, mousePos.y, '');
            processProperties.process = pro;
            processProperties.show();
            pro.dragging = true;
            mainContainer.addChild(pro);
        });

        this.addChild(button);
    }

    addExecutorButton() {
        let textureButton = PIXI.Loader.shared.resources.cooker.texture.clone();
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.cookerOver.texture.clone();
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let button = new Button(textureButton, textureButtonOver, textureButtonOver, this.buttonMargin, 2*this.buttonMargin + this.buttonSize.h, this.buttonSize.w, this.buttonSize.h, () => {
            console.log(`Added! For now we just support single thread executors... java.util.concurrent.Executors$FinalizableDelegatedExecutorService`);
            app.workflowData.executorServices.push('java.util.concurrent.Executors$FinalizableDelegatedExecutorService');
            saveWorkflow();
        });

        this.addChild(button);
    }

    addExecuteButton() {
        let textureButton = PIXI.Loader.shared.resources.play.texture.clone();
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.playOver.texture.clone();
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let button = new Button(textureButton, textureButtonOver, textureButtonOver, this.buttonMargin, 3*this.buttonMargin + 2*this.buttonSize.h, this.buttonSize.w, this.buttonSize.h, async () => {
            console.log(`It should now run the workflow on the server.`);
            
            let startingPoints = [];

            await Object.getOwnPropertyNames(app.workflowData.processes).forEach(pId => {
                if(app.workflowData.processes[pId] != undefined && app.workflowData.processes[pId].binders != undefined && 
                    app.workflowData.processes[pId].binders.input != undefined && app.workflowData.processes[pId].binders.input.length == 0){
                        startingPoints.push(pId);
                    }
            });

            console.log(startingPoints);
            
            Server.postToServer(startingPoints, `admin/start`, (data, status) => {
                console.log(`Workflow executed!`);
            }, (jqXHR, status, err) => {
                console.error(`Couldn't execute this workflow: ${err}`);
            });
        });

        this.addChild(button);
    }
}

export { Navbar };