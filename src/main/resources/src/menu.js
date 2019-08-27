import * as PIXI from 'pixi.js'
import { Binder } from './modules/PIXI/Binder';
import { Process } from './modules/PIXI/Process';
import { mainContainer, app } from './index';
import { binderProperties } from './modules/html/BinderProperties';
import { processProperties } from './modules/html/ProcessProperties';


const menu = [
    {
        button: ['Bind', 'btn-primary'], action: function (e) {
            if (this.binder != undefined) { }
            else {
                if (this.lastProcess != undefined && this.lastProcess instanceof Process) {
                    if (this.lastProcess == this.process)
                        console.warn(`You should bind two different processes...`);
                    else {
                        binderProperties.process1 = this.lastProcess;
                        binderProperties.process2 = this.process;

                        let b = new Binder(this.lastProcess, this.process);
                        mainContainer.addChild(b);

                        binderProperties.binder = b;
                        binderProperties.show();
                        this.process.addInputBinder(b);
                        this.lastProcess.addOutputBinder(b);
                    }
                }
                this.lastProcess = this.process;
            }
        }
    },
    {
        button: ['Properties', 'btn-info'], action: function () {
            if (this.process == undefined && this.binder != undefined) {
                binderProperties.binder = this.binder;
                binderProperties.process1 = this.binder.origProcess;
                binderProperties.process2 = this.binder.destProcess;
                binderProperties.show();
            } else if (this.process != undefined) {
                processProperties.process = this.process;
                processProperties.show();
            }
        }
    },
    {
        button: ['Remove', 'btn-danger'], action: function () {
            if (this.process != undefined) this.process.destroy();
            if (this.binder != undefined) this.binder.destroy();
        }
    },
    {
        button: ['X', 'btn-light'], action: function () {
            this.nav.hide();
        }
    }
];

export { menu };