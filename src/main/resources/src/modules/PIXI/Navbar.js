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

        this.x = 0;
        this.y = 0;
        this.width = 50;
        this.height = window.innerHeight;

        this.addProcessButton();
        this.addExecutorButton();
        this.addExecuteButton();
    }

    addProcessButton() {
        let textureButton = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButton.frame = new PIXI.Rectangle(0, 158, 39, 33);
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButtonOver.frame = new PIXI.Rectangle(40, 157, 39, 33);
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

    addExecutorButton() {
        let textureButton = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButton.frame = new PIXI.Rectangle(82, 158, 38, 33);
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButtonOver.frame = new PIXI.Rectangle(123, 157, 38, 33);
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation


        let button = new Button(textureButton, textureButtonOver, textureButtonOver, 4, 41, 38, 35, () => {
            console.log(`Added! For now we just support single thread executors... java.util.concurrent.Executors$FinalizableDelegatedExecutorService`);
            app.workflowData.executorServices.push('java.util.concurrent.Executors$FinalizableDelegatedExecutorService');
            saveWorkflow();
        });

        this.addChild(button);
    }

    addExecuteButton() {
        let textureButton = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButton.frame = new PIXI.Rectangle(165, 158, 37, 33);
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.tileset.texture.clone();
        textureButtonOver.frame = new PIXI.Rectangle(206, 157, 37, 33);
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation


        let button = new Button(textureButton, textureButtonOver, textureButtonOver, 4, 79, 37, 35, async () => {
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