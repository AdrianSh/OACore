import * as PIXI from 'pixi.js';
import { navbar } from '../html/Navbar';
import Server from '../Server'
import Button from './Button'
import { app, saveWorkflow } from '../../index'
import { binderProperties } from './../html/BinderProperties'


export class Binder extends Button {
    constructor(x, y, callback, process) {
        let textureButton = PIXI.Loader.shared.resources.gear.texture.clone();
        textureButton.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        let textureButtonOver = PIXI.Loader.shared.resources.gearOver.texture.clone();
        textureButtonOver.baseTexture.scaleMode = PIXI.SCALE_MODES.NEAREST; // Scale mode for pixelation

        super(textureButton, textureButtonOver, textureButtonOver, x, y, 24, 24, () => {
            console.log(`Click on binder....`);
            callback();
            binderProperties.binderData = this.binderData;
            binderProperties.binderButton = this;
            binderProperties.show();
        });

        this.process = process;
        this.interactive = true;
        this.buttonMode = true;

        this.binderData = {
            binderType: 'es.jovenesadventistas.arnion.process.binders.RunnableBinder',
            runnableType: 'Void',
            runnableId: '0'
        };
    }
}